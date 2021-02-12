package org.pytorch.demo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class InfoViewFactory {

  public static final int INFO_VIEW_TYPE_IMAGE_CLASSIFICATION_CIFAR10 = 1;
  public static final int INFO_VIEW_TYPE_IMAGE_CLASSIFICATION_CIFAR100 = 2;
  public static final int INFO_VIEW_TYPE_IMAGE_CLASSIFICATION_IMAGENET = 3;
  public static final int INFO_VIEW_TYPE_IMAGE_CLASSIFICATION_ISIC = 4;


  public static View newInfoView(Context context, int infoViewType, @Nullable String additionalText) {

    LayoutInflater inflater = LayoutInflater.from(context);
    View view = inflater.inflate(R.layout.info, null, false);
    TextView infoTextView = view.findViewById(R.id.info_title);
    infoTextView.setText(R.string.vision_card_info_title);


    if (INFO_VIEW_TYPE_IMAGE_CLASSIFICATION_CIFAR10 == infoViewType) {
      TextView descriptionTextView = view.findViewById(R.id.info_description);
      StringBuilder sb = new StringBuilder(context.getString(R.string.vision_card_cifar10_description));
      if (additionalText != null) {
        sb.append('\n').append("Model : ").append(additionalText);
      }
      descriptionTextView.setText(sb.toString());
      return view;
    }
    else if (INFO_VIEW_TYPE_IMAGE_CLASSIFICATION_CIFAR100 == infoViewType) {

      TextView descriptionTextView = view.findViewById(R.id.info_description);
      StringBuilder sb = new StringBuilder(context.getString(R.string.vision_card_cifar100_description));
      if (additionalText != null) {
        sb.append('\n').append("Model : ").append(additionalText);
      }
      descriptionTextView.setText(sb.toString());
      return view;
    }
    else if (INFO_VIEW_TYPE_IMAGE_CLASSIFICATION_IMAGENET == infoViewType) {

        TextView descriptionTextView = view.findViewById(R.id.info_description);
        StringBuilder sb = new StringBuilder(context.getString(R.string.vision_card_imagenet_description));
        if (additionalText != null) {
          sb.append('\n').append("Model : ").append(additionalText);
        }
        descriptionTextView.setText(sb.toString());
        return view;
      }
    else if (INFO_VIEW_TYPE_IMAGE_CLASSIFICATION_ISIC == infoViewType) {

      TextView descriptionTextView = view.findViewById(R.id.info_description);
      StringBuilder sb = new StringBuilder(context.getString(R.string.vision_card_isic_description));
      if (additionalText != null) {
        sb.append('\n').append("Model : ").append(additionalText);
      }
      descriptionTextView.setText(sb.toString());
      return view;
    }
    throw new IllegalArgumentException("Unknown info view type");
  }

  public static View newErrorDialogView(Context context) {
    LayoutInflater inflater = LayoutInflater.from(context);
    View view = inflater.inflate(R.layout.error_dialog, null, false);
    return view;
  }
}
