package org.pytorch.demo.vision;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.DownloadListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;


import org.pytorch.demo.AbstractListActivity;
import org.pytorch.demo.Constants;
import org.pytorch.demo.InfoViewFactory;
import org.pytorch.demo.R;
import org.pytorch.demo.Utils;
import org.pytorch.demo.log.ModelsCollector;

import java.io.FileWriter;
import java.io.IOException;

import lib.folderpicker.FolderPicker;


public class VisionListActivity extends AbstractListActivity {
  private static final int FOLDERPICKER_CODE = 9999;


  public static CheckBox checkBox;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    TextView tv1 = (TextView)findViewById(R.id.path_to_models);
    tv1.setText("Models path : " + ModelsCollector.getPathToModels(this));

    Spinner spinner = findViewById(R.id.spinner);
    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ModelsCollector.getModels(this));
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);
    Context c = this;
    ImageView imgFavorite = (ImageView) findViewById(R.id.refresh_button);
    imgFavorite.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        adapter.clear();
        adapter.addAll(ModelsCollector.getModels(c));
        tv1.setText("Models path : " + ModelsCollector.getPathToModels(c));
      }
    });

    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() //Download models
    {
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
      {
        String selectedItem = parent.getItemAtPosition(position).toString();
        if(selectedItem.equals("Choose a model")){
          //do nothing
        }
        if(selectedItem.equals("Download models.."))
        {
          String url = "https://drive.google.com/drive/folders/1PxQs2twCW5U6v3iu5m1znNz3Z096EG_v?usp=sharing";

          Intent i = new Intent(Intent.ACTION_VIEW);
          i.setData(Uri.parse(url));
          startActivity(i);
        }

      }
      public void onNothingSelected(AdapterView<?> parent) {}
    });



    findViewById(R.id.start_click_area).setOnClickListener(v -> {
      final Intent intent = new Intent(VisionListActivity.this, ImageClassificationActivity.class);
      intent.putExtra(ImageClassificationActivity.INTENT_MODULE_ASSET_NAME, (String) spinner.getSelectedItem());
      String nModuleAssetName = (String) spinner.getSelectedItem();
      //String modelSelected = nModuleAssetName.substring(nModuleAssetName.indexOf('_')+1,nModuleAssetName.indexOf('.')); //MODEL PATHNAME = [DATASET]_[ARCHITECTURE]_[TECHNIQUE].pt
      //switch(modelSelected.substring(0,modelSelected.indexOf('_')))
      //{
      if(nModuleAssetName.indexOf("cifar100") != -1) {
        //case "alexnet":
        intent.putExtra(ImageClassificationActivity.INTENT_INPUT_TENSOR_WIDTH, 32);
        intent.putExtra(ImageClassificationActivity.INTENT_INPUT_TENSOR_HEIGHT, 32);
        intent.putExtra(ImageClassificationActivity.INTENT_INPUT_DATASET, Constants.CIFAR100_CLASSES);
        intent.putExtra(ImageClassificationActivity.INTENT_INFO_VIEW_TYPE, InfoViewFactory.INFO_VIEW_TYPE_IMAGE_CLASSIFICATION_CIFAR100);
        intent.putExtra(ImageClassificationActivity.INTENT_NORM_MEAN_RGB, new float[]{0.507f, 0.486f, 0.441f});
        intent.putExtra(ImageClassificationActivity.INTENT_NORM_STD_RGB, new float[]{0.267f, 0.256f, 0.276f});
        //break;
      }
      else if(nModuleAssetName.indexOf("cifar10") != -1) {
        //case "resnet32":
        //case "vgg162l":
          intent.putExtra(ImageClassificationActivity.INTENT_INPUT_TENSOR_WIDTH, 32);
          intent.putExtra(ImageClassificationActivity.INTENT_INPUT_TENSOR_HEIGHT, 32);
          intent.putExtra(ImageClassificationActivity.INTENT_INPUT_DATASET, Constants.CIFAR10_CLASSES);
          intent.putExtra(ImageClassificationActivity.INTENT_INFO_VIEW_TYPE, InfoViewFactory.INFO_VIEW_TYPE_IMAGE_CLASSIFICATION_CIFAR10);
          intent.putExtra(ImageClassificationActivity.INTENT_NORM_MEAN_RGB, new float[]{0.491f, 0.482f, 0.446f});
          intent.putExtra(ImageClassificationActivity.INTENT_NORM_STD_RGB, new float[]{0.202f, 0.199f, 0.201f});
          //break;
      }
      else if(nModuleAssetName.indexOf("isic") != -1) {
        //default:
        intent.putExtra(ImageClassificationActivity.INTENT_INPUT_TENSOR_WIDTH,224);
        intent.putExtra(ImageClassificationActivity.INTENT_INPUT_TENSOR_HEIGHT,224);
        intent.putExtra(ImageClassificationActivity.INTENT_INPUT_DATASET,Constants.ISIC_CLASSES);
        intent.putExtra(ImageClassificationActivity.INTENT_INFO_VIEW_TYPE, InfoViewFactory.INFO_VIEW_TYPE_IMAGE_CLASSIFICATION_IMAGENET);
        intent.putExtra(ImageClassificationActivity.INTENT_NORM_MEAN_RGB, new float[]{0.6681f, 0.5301f, 0.5247f});
        intent.putExtra(ImageClassificationActivity.INTENT_NORM_STD_RGB, new float[]{0.1337f, 0.1480f, 0.1595f});
      }
      else {
        //default:
          intent.putExtra(ImageClassificationActivity.INTENT_INPUT_TENSOR_WIDTH,224);
          intent.putExtra(ImageClassificationActivity.INTENT_INPUT_TENSOR_HEIGHT,224);
          intent.putExtra(ImageClassificationActivity.INTENT_INPUT_DATASET,Constants.IMAGENET_CLASSES);
          intent.putExtra(ImageClassificationActivity.INTENT_INFO_VIEW_TYPE, InfoViewFactory.INFO_VIEW_TYPE_IMAGE_CLASSIFICATION_IMAGENET);
          intent.putExtra(ImageClassificationActivity.INTENT_NORM_MEAN_RGB, new float[]{0.485f, 0.456f, 0.406f});
          intent.putExtra(ImageClassificationActivity.INTENT_NORM_STD_RGB, new float[]{0.229f, 0.224f, 0.225f});
      }
      startActivity(intent);
    });

    checkBox = findViewById(R.id.checkBox);

    findViewById(R.id.path_models_button).setOnClickListener(v -> {
      Intent intent = new Intent(this, FolderPicker.class);
      intent.putExtra("title", "Select models directory");
      startActivityForResult(intent, FOLDERPICKER_CODE);
    });

  }

  @Override
  protected int getListContentLayoutRes() {
    return R.layout.vision_list_content;
  }

  public static boolean check_log(){

    return checkBox.isChecked();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent) { //folderpicker
    super.onActivityResult(requestCode, resultCode, intent);
    if (requestCode == FOLDERPICKER_CODE && resultCode == Activity.RESULT_OK) {

      String folderLocation = intent.getExtras().getString("data");
      System.out.println("LOG : " + folderLocation);
      try {
        FileWriter fw = new FileWriter(Utils.assetFilePath(this,"path_to_models.txt"));
        fw.write(folderLocation);
        fw.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
