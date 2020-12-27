package org.pytorch.demo.vision;

import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.demo.Constants;
import org.pytorch.demo.R;
import org.pytorch.demo.Utils;
import org.pytorch.demo.log.ModelsCollector;
import org.pytorch.demo.vision.view.ResultRowView;
import org.pytorch.torchvision.TensorImageUtils;

import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.ImageProxy;

import static org.pytorch.demo.vision.VisionListActivity.check_log;

public class ImageClassificationActivity extends AbstractCameraXActivity<ImageClassificationActivity.AnalysisResult> {

  public static final String INTENT_MODULE_ASSET_NAME = "INTENT_MODULE_ASSET_NAME";
  public static final String INTENT_INFO_VIEW_TYPE = "INTENT_INFO_VIEW_TYPE";
  public static final String INTENT_INPUT_TENSOR_WIDTH = "INPUT_TENSOR_WIDTH";
  public static final String INTENT_INPUT_TENSOR_HEIGHT = "INPUT_TENSOR_HEIGHT ";
  public static final String INTENT_INPUT_DATASET = "INTENT_INPUT_DATASET";
  public static final String INTENT_NORM_MEAN_RGB = "INTENT_NORM_MEAN_RGB";
  public static final String INTENT_NORM_STD_RGB = "INTENT_NORM_STD_RGB";


  private static final int TOP_K = 3;
  private static final int MOVING_AVG_PERIOD = 10;
  private static final String FORMAT_MS = "%dms";
  private static final String FORMAT_AVG_MS = "avg:%.0fms";

  private static final String FORMAT_FPS = "%.1fFPS";
  public static final String SCORES_FORMAT = "%.2f";

  static class AnalysisResult {

    private final String[] topNClassNames;
    private final float[] topNScores;
    private final long analysisDuration;
    private final long moduleForwardDuration;

    public AnalysisResult(String[] topNClassNames, float[] topNScores,
                          long moduleForwardDuration, long analysisDuration) {
      this.topNClassNames = topNClassNames;
      this.topNScores = topNScores;
      this.moduleForwardDuration = moduleForwardDuration;
      this.analysisDuration = analysisDuration;
    }
  }

  private boolean mAnalyzeImageErrorState;
  private ResultRowView[] mResultRowViews = new ResultRowView[TOP_K];
  private TextView mFpsText;
  private TextView mMsText;
  private TextView mMsAvgText;
  private Module mModule;
  private String mModuleAssetName;
  private FloatBuffer mInputTensorBuffer;
  private Tensor mInputTensor;
  private long mMovingAvgSum = 0;
  private Queue<Long> mMovingAvgQueue = new LinkedList<>();
  private String[] dataset;
  private int inputTensorWidth;
  private int inputTensorHeight;
  private float[] meanRGB;
  private float[] stdRGB;


  @Override
  protected int getContentViewLayoutId() {
    return R.layout.activity_image_classification;
  }

  @Override
  protected TextureView getCameraPreviewTextureView() {
    return ((ViewStub) findViewById(R.id.image_classification_texture_view_stub))
        .inflate()
        .findViewById(R.id.image_classification_texture_view);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    final ResultRowView headerResultRowView =
        findViewById(R.id.image_classification_result_header_row);
    headerResultRowView.nameTextView.setText(R.string.image_classification_results_header_row_name);
    headerResultRowView.scoreTextView.setText(R.string.image_classification_results_header_row_score);

    mResultRowViews[0] = findViewById(R.id.image_classification_top1_result_row);
    mResultRowViews[1] = findViewById(R.id.image_classification_top2_result_row);
    mResultRowViews[2] = findViewById(R.id.image_classification_top3_result_row);

    mFpsText = findViewById(R.id.image_classification_fps_text);
    mMsText = findViewById(R.id.image_classification_ms_text);
    mMsAvgText = findViewById(R.id.image_classification_ms_avg_text);

  }

  @Override
  protected void applyToUiAnalyzeImageResult(AnalysisResult result) {
    mMovingAvgSum += result.moduleForwardDuration;
    mMovingAvgQueue.add(result.moduleForwardDuration);
    if (mMovingAvgQueue.size() > MOVING_AVG_PERIOD) {
      mMovingAvgSum -= mMovingAvgQueue.remove();
    }

    for (int i = 0; i < TOP_K; i++) {
      final ResultRowView rowView = mResultRowViews[i];
      rowView.nameTextView.setText(result.topNClassNames[i]);
      rowView.scoreTextView.setText(String.format(Locale.US, SCORES_FORMAT,
          result.topNScores[i]));
      rowView.setProgressState(false);
    }

    mMsText.setText(String.format(Locale.US, FORMAT_MS, result.moduleForwardDuration));
    if (mMsText.getVisibility() != View.VISIBLE) {
      mMsText.setVisibility(View.VISIBLE);
    }
    mFpsText.setText(String.format(Locale.US, FORMAT_FPS, (1000.f / result.analysisDuration)));
    if (mFpsText.getVisibility() != View.VISIBLE) {
      mFpsText.setVisibility(View.VISIBLE);
    }

    if (mMovingAvgQueue.size() == MOVING_AVG_PERIOD) {
      float avgMs = (float) mMovingAvgSum / MOVING_AVG_PERIOD;
      mMsAvgText.setText(String.format(Locale.US, FORMAT_AVG_MS, avgMs));
      if (mMsAvgText.getVisibility() != View.VISIBLE) {
        mMsAvgText.setVisibility(View.VISIBLE);
      }
    }

    Toolbar mActionBarToolbar = findViewById(R.id.toolbar);
    String moduleSelected = mModuleAssetName.substring(mModuleAssetName.indexOf('_')+1,mModuleAssetName.indexOf('.'));
    mActionBarToolbar.setTitle(moduleSelected);

  }


  protected String getModuleAssetName() {
    if (!TextUtils.isEmpty(mModuleAssetName)) {
      return mModuleAssetName;
    }
    final String moduleAssetNameFromIntent = getIntent().getStringExtra(INTENT_MODULE_ASSET_NAME);
    mModuleAssetName = !TextUtils.isEmpty(moduleAssetNameFromIntent)
        ? moduleAssetNameFromIntent
        : "null";

    return mModuleAssetName;
  }


  public int getInputTensorWidth() {
    if (inputTensorWidth != 0) {
      return inputTensorWidth;
    }
    final int intentInputTensorWidth = getIntent().getIntExtra(INTENT_INPUT_TENSOR_WIDTH,10);
    inputTensorWidth = intentInputTensorWidth;

    return inputTensorWidth;
  }

  public int getInputTensorHeight() {
    if (inputTensorHeight != 0) {
      return inputTensorHeight;
    }
    final int intentInputTensorHeight = getIntent().getIntExtra(INTENT_INPUT_TENSOR_HEIGHT,10);
    inputTensorHeight = intentInputTensorHeight;

    return inputTensorHeight;
  }

  protected String[] getIntentInputDataset() {
    if (dataset!=null) {
      return dataset;
    }
    final String[] intentDataset = getIntent().getStringArrayExtra(INTENT_INPUT_DATASET);
    dataset = intentDataset;

    return dataset;
  }

  protected float[] getIntentMEANDataset() {
    if (meanRGB!=null) {
      return meanRGB;
    }
    final float[] intentMEAN = getIntent().getFloatArrayExtra(INTENT_NORM_MEAN_RGB);
    meanRGB = intentMEAN;

    return meanRGB;
  }

  protected float[] getIntentSTDDataset() {
    if (stdRGB!=null) {
      return stdRGB;
    }
    final float[] intentSTD = getIntent().getFloatArrayExtra(INTENT_NORM_STD_RGB);
    stdRGB = intentSTD;

    return stdRGB;
  }


  @Override
  protected String getInfoViewAdditionalText() {
    return getModuleAssetName();
  }

  @Override
  @WorkerThread
  @Nullable
  protected AnalysisResult analyzeImage(ImageProxy image, int rotationDegrees) {
    if (mAnalyzeImageErrorState) {
      return null;
    }

    try {
      if (mModule == null) {

        mModule = Module.load(ModelsCollector.getPathToModels(this) + "/" + getModuleAssetName());
        dataset = getIntentInputDataset();
        inputTensorWidth = getInputTensorWidth();
        inputTensorHeight = getInputTensorHeight();

        mInputTensorBuffer =
            Tensor.allocateFloatBuffer(3 * inputTensorWidth * inputTensorHeight);
        mInputTensor = Tensor.fromBlob(mInputTensorBuffer, new long[]{1,3, inputTensorHeight, inputTensorWidth});
      }

      final long startTime = SystemClock.elapsedRealtime();
      TensorImageUtils.imageYUV420CenterCropToFloatBuffer(
          image.getImage(), rotationDegrees,
              inputTensorWidth, inputTensorHeight,
              getIntentMEANDataset(),
          //TensorImageUtils.TORCHVISION_NORM_STD_RGB,
              getIntentSTDDataset(),
          mInputTensorBuffer, 0);

      final long moduleForwardStartTime = SystemClock.elapsedRealtimeNanos();
      final Tensor outputTensor = mModule.forward(IValue.from(mInputTensor)).toTensor();
      final long moduleForwardDuration = SystemClock.elapsedRealtimeNanos() - moduleForwardStartTime;

      final float[] scores = outputTensor.getDataAsFloatArray();
      final int[] ixs = Utils.topK(scores, TOP_K);

      final String[] topKClassNames = new String[TOP_K];
      final float[] topKScores = new float[TOP_K];
      for (int i = 0; i < TOP_K; i++) {
        final int ix = ixs[i];
        topKClassNames[i] = dataset[ix];
        topKScores[i] = scores[ix];
      }
      final long analysisDuration = SystemClock.elapsedRealtime() - startTime;

      String moduleSelected = mModuleAssetName.substring(mModuleAssetName.indexOf('_')+1,mModuleAssetName.indexOf('.'));

      if(check_log()) //collect stats CheckBox (ON)
        getStatsCollector().log(moduleSelected,moduleForwardDuration/(double)1000,(1000.f/analysisDuration));


      return new AnalysisResult(topKClassNames, topKScores, moduleForwardDuration/1000000, analysisDuration);
    } catch (Exception e) {
      Log.e(Constants.TAG, "Error during image analysis", e);
      mAnalyzeImageErrorState = true;
      runOnUiThread(() -> {
        if (!isFinishing()) {
          showErrorDialog(v -> ImageClassificationActivity.this.finish());
        }
      });
      return null;
    }
  }

  @Override
  protected int getInfoViewCode() {
    return getIntent().getIntExtra(INTENT_INFO_VIEW_TYPE, -1);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mModule != null) {
      mModule.destroy();
    }
  }
}
