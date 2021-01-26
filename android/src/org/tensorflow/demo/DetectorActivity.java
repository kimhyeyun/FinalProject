/*
 * Copyright 2016 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.demo;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import org.tensorflow.demo.OverlayView.DrawCallback;
import org.tensorflow.demo.env.BorderedText;
import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.demo.env.Logger;
import org.tensorflow.demo.tracking.MultiBoxTracker;
import org.tensorflow.demo.R; // Explicit import needed for internal Google builds.

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.googlecode.tesseract.android.TessBaseAPI;
/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {
  private static final Logger LOGGER = new Logger();

  //AssetManager am = getResources().getAssets() ;

  // Configuration values for the prepackaged multibox model.
  private static final int MB_INPUT_SIZE = 224;
  private static final int MB_IMAGE_MEAN = 128;
  private static final float MB_IMAGE_STD = 128;
  private static final String MB_INPUT_NAME = "ResizeBilinear";
  private static final String MB_OUTPUT_LOCATIONS_NAME = "output_locations/Reshape";
  private static final String MB_OUTPUT_SCORES_NAME = "output_scores/Reshape";
  private static final String MB_MODEL_FILE = "file:///android_asset/multibox_model.pb";
  private static final String MB_LOCATION_FILE = "file:///android_asset/multibox_location_priors.txt";

  private static final int TF_OD_API_INPUT_SIZE = 300;
  private static final String TF_OD_API_MODEL_FILE = "file:///android_asset/ssd_mobilenet_v1_android_export.pb";
  private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/coco_labels_list.txt";

  // Configuration values for tiny-yolo-voc. Note that the graph is not included with TensorFlow and
  // must be manually placed in the assets/ directory by the user.
  // Graphs and models downloaded from http://pjreddie.com/darknet/yolo/ may be converted e.g. via
  // DarkFlow (https://github.com/thtrieu/darkflow). Sample command:
  // ./flow --model cfg/tiny-yolo-voc.cfg --load bin/tiny-yolo-voc.weights --savepb --verbalise
  private static final String YOLO_MODEL_FILE = "file:///android_asset/my-tiny-yolo.pb";
  //private static final String YOLO_MODEL_FILE = "./android_asset/my-tiny-yolo.pb";

  private static final int YOLO_INPUT_SIZE = 416;
  private static final String YOLO_INPUT_NAME = "input";
  private static final String YOLO_OUTPUT_NAMES = "output";
  private static final int YOLO_BLOCK_SIZE = 32;

  // Which detection model to use: by default uses Tensorflow Object Detection API frozen
  // checkpoints.  Optionally use legacy Multibox (trained using an older version of the API)
  // or YOLO.
  private enum DetectorMode {
    TF_OD_API, MULTIBOX, YOLO;
  }
  private static final DetectorMode MODE = DetectorMode.YOLO;

  // Minimum detection confidence to track a detection.
  private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.6f;
  private static final float MINIMUM_CONFIDENCE_MULTIBOX = 0.1f;
  private static final float MINIMUM_CONFIDENCE_YOLO = 0.5f;
  //private static final float MINIMUM_CONFIDENCE_YOLO = 0.05f;

  private static final boolean MAINTAIN_ASPECT = MODE == DetectorMode.YOLO;

  private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);

  private static final boolean SAVE_PREVIEW_BITMAP = false;
  private static final float TEXT_SIZE_DIP = 10;

  private Integer sensorOrientation;

  private Classifier detector;

  private long lastProcessingTimeMs;
  private Bitmap rgbFrameBitmap = null;
  private Bitmap croppedBitmap = null;
  private Bitmap cropCopyBitmap = null;

  private boolean computingDetection = false;

  private long timestamp = 0;

  private Matrix frameToCropTransform;
  private Matrix cropToFrameTransform;

  private MultiBoxTracker tracker;

  private byte[] luminanceCopy;

  private BorderedText borderedText;


  //추가한부분
  private String text2 ;
  private String[] list1 = new String[3];
  TextToSpeech tts;
  private Button button01, button02;
  boolean pause = false;
  TessBaseAPI tessBaseAPI;
  CameraSurfaceView surfaceView;
  Button buttontext;

  TextView textview;
  private long btnPressTime = 0;
  RelativeLayout relative_camera;

  //





  @Override
  public void onPreviewSizeChosen(final Size size, final int rotation) {
    Log.d("1", "onPreviewSizeChosen");

    //추가한부분

    tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
      @Override
      public void onInit(int status) {
        if(status != TextToSpeech.ERROR) {
          tts.setLanguage(Locale.KOREA);
        }
      }
    });



    //






    final float textSizePx =
            TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
    borderedText.setTypeface(Typeface.MONOSPACE);

    tracker = new MultiBoxTracker(this);

    int cropSize = TF_OD_API_INPUT_SIZE;
    if (MODE == DetectorMode.YOLO) {
      detector =
              TensorFlowYoloDetector.create(
                      getAssets(),
                      YOLO_MODEL_FILE,
                      YOLO_INPUT_SIZE,
                      YOLO_INPUT_NAME,
                      YOLO_OUTPUT_NAMES,
                      YOLO_BLOCK_SIZE);
      cropSize = YOLO_INPUT_SIZE;
    } else if (MODE == DetectorMode.MULTIBOX) {
      detector =
              TensorFlowMultiBoxDetector.create(
                      getAssets(),
                      MB_MODEL_FILE,
                      MB_LOCATION_FILE,
                      MB_IMAGE_MEAN,
                      MB_IMAGE_STD,
                      MB_INPUT_NAME,
                      MB_OUTPUT_LOCATIONS_NAME,
                      MB_OUTPUT_SCORES_NAME);
      cropSize = MB_INPUT_SIZE;
    } else {
      try {
        detector = TensorFlowObjectDetectionAPIModel.create(
                getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
        cropSize = TF_OD_API_INPUT_SIZE;
      } catch (final IOException e) {
        LOGGER.e(e, "Exception initializing classifier!");
        Toast toast =
                Toast.makeText(
                        getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
        toast.show();
        finish();
      }
    }

    previewWidth = size.getWidth();
    previewHeight = size.getHeight();

    sensorOrientation = rotation - getScreenOrientation();
    LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

    LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
    croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);

    frameToCropTransform =
            ImageUtils.getTransformationMatrix(
                    previewWidth, previewHeight,
                    cropSize, cropSize,
                    sensorOrientation, MAINTAIN_ASPECT);

    cropToFrameTransform = new Matrix();
    frameToCropTransform.invert(cropToFrameTransform);

    trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
    trackingOverlay.addCallback(
            new DrawCallback() {
              @Override
              public void drawCallback(final Canvas canvas) {
                tracker.draw(canvas);
                if (isDebug()) {
                  tracker.drawDebug(canvas);
                }
              }
            });

    addCallback(
            new DrawCallback() {
              @Override
              public void drawCallback(final Canvas canvas) {
                Log.d("2", "drawCallback");
                if (!isDebug()) {
                  return;
                }
                final Bitmap copy = cropCopyBitmap;
                if (copy == null) {
                  return;
                }

                final int backgroundColor = Color.argb(100, 0, 0, 0);
                canvas.drawColor(backgroundColor);

                final Matrix matrix = new Matrix();
                final float scaleFactor = 2;
                matrix.postScale(scaleFactor, scaleFactor);
                matrix.postTranslate(
                        canvas.getWidth() - copy.getWidth() * scaleFactor,
                        canvas.getHeight() - copy.getHeight() * scaleFactor);
                canvas.drawBitmap(copy, matrix, new Paint());

                final Vector<String> lines = new Vector<String>();
                if (detector != null) {
                  final String statString = detector.getStatString();
                  final String[] statLines = statString.split("\n");
                  for (final String line : statLines) {
                    lines.add(line);
                  }
                }
                lines.add("");

                lines.add("Frame: " + previewWidth + "x" + previewHeight);
                lines.add("Crop: " + copy.getWidth() + "x" + copy.getHeight());
                lines.add("View: " + canvas.getWidth() + "x" + canvas.getHeight());
                lines.add("Rotation: " + sensorOrientation);
                lines.add("Inference time: " + lastProcessingTimeMs + "ms");

                borderedText.drawLines(canvas, 10, canvas.getHeight() - 10, lines);

                // if(lines.)








              }
            });
  }

  OverlayView trackingOverlay;

  @Override
  protected void processImage() {
    Log.d("3", "processImage");
    ++timestamp;
    final long currTimestamp = timestamp;
    byte[] originalLuminance = getLuminance();
    tracker.onFrame(
            previewWidth,
            previewHeight,
            getLuminanceStride(),
            sensorOrientation,
            originalLuminance,
            timestamp);
    trackingOverlay.postInvalidate();

    // No mutex needed as this method is not reentrant.
    if (computingDetection) {
      readyForNextImage();
      return;
    }
    computingDetection = true;
    LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

    rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

    if (luminanceCopy == null) {
      luminanceCopy = new byte[originalLuminance.length];
    }
    System.arraycopy(originalLuminance, 0, luminanceCopy, 0, originalLuminance.length);
    readyForNextImage();

    final Canvas canvas = new Canvas(croppedBitmap);
    canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
    // For examining the actual TF input.
    if (SAVE_PREVIEW_BITMAP) {
      ImageUtils.saveBitmap(croppedBitmap);
    }

    runInBackground(
            new Runnable() {
              @Override
              public void run() {
                Log.d("4", "run");
                LOGGER.i("Running detection on image " + currTimestamp);
                final long startTime = SystemClock.uptimeMillis();
                final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
                lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
                final Canvas canvas = new Canvas(cropCopyBitmap);
                final Paint paint = new Paint();
                paint.setColor(Color.RED);
                paint.setStyle(Style.STROKE);
                paint.setStrokeWidth(2.0f);

                float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                switch (MODE) {
                  case TF_OD_API:
                    minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                    break;
                  case MULTIBOX:
                    minimumConfidence = MINIMUM_CONFIDENCE_MULTIBOX;
                    break;
                  case YOLO:
                    minimumConfidence = MINIMUM_CONFIDENCE_YOLO;
                    break;
                }

                final List<Classifier.Recognition> mappedRecognitions =
                        new LinkedList<Classifier.Recognition>();

                //추가
                int i = 0;
                //

                for (final Classifier.Recognition result : results) {
                  final RectF location = result.getLocation();
                  if (location != null && result.getConfidence() >= minimumConfidence) {
                    canvas.drawRect(location, paint);

                    cropToFrameTransform.mapRect(location);
                    result.setLocation(location);
                    mappedRecognitions.add(result);



                    //추가한부분



                    String str = result.getTitle();
                    if(str.equals("trafficlightGreen")) {
                      tts.playSilentUtterance(5000, TextToSpeech.QUEUE_ADD, null);

                      list1[i] = "초록불입니다,,,,,,,,,,,,,,,,,";
                      tts.playSilentUtterance(5000, TextToSpeech.QUEUE_ADD, null);


                    }
                    else if(str.equals("trafficlightRed")) {
                      tts.playSilentUtterance(5000, TextToSpeech.QUEUE_ADD, null);

                      list1[i] = "빨간불입니다,,,,,,,,,,,,,,,,,";
                      tts.playSilentUtterance(5000, TextToSpeech.QUEUE_ADD, null);

                      //tts.playSilentUtterance(5000, TextToSpeech.QUEUE_ADD, null);
                    }



                    else {
                      list1[i] = "     신호등입니다   ";
                      //tts.playSilentUtterance(5000, TextToSpeech.QUEUE_ADD, null);
                    }


                    if(i==2) {i = 0;}
                    else{i++;}







                  }
                }

                tts.setSpeechRate(0.9f);

                Handler delayHandler = new Handler();
                delayHandler.postDelayed(new Runnable() {
                  @Override
                  public void run() {


                    if(pause == false){
                      tts.playSilence(3000, TextToSpeech.QUEUE_ADD, null);

                      tts.speak(list1[0], TextToSpeech.QUEUE_FLUSH, null, null);
                      tts.playSilence(3000, TextToSpeech.QUEUE_ADD, null);


                    }
                    else{
                      tts.stop();
                    }


                  }
                },3000);





                tracker.trackResults(mappedRecognitions, luminanceCopy, currTimestamp);
                trackingOverlay.postInvalidate();

                requestRender();
                computingDetection = false;
              }
            });
  }

  @Override
  protected int getLayoutId() {
    Log.d("5", "getLayoutId");
    return R.layout.camera_connection_fragment_tracking;
  }

  @Override
  protected Size getDesiredPreviewFrameSize() {
    Log.d("6", "getDesiredPreviewFrameSize");
    return DESIRED_PREVIEW_SIZE;
  }

  @Override
  public void onSetDebug(final boolean debug) {
    Log.d("7", "onSetDebug");
    detector.enableStatLogging(debug);
  }




//@@@@@@@아래 추가한 부분
//////////////////////////////////////////////////////////

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_camera);

    button01 = (Button) findViewById(R.id.button01);

    button02 = (Button) findViewById(R.id.button02);

    relative_camera = (RelativeLayout) findViewById((R.id.relative_camera));


    button01.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        pause= false;
        //  tts.speak(list1[0], TextToSpeech.QUEUE_FLUSH, null, null);
      }
    });

    button02.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        pause = true;
      }
    });



    relative_camera.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        if(System.currentTimeMillis()>btnPressTime+1000){
          btnPressTime = System.currentTimeMillis();
          String text2 = "카메라";
          Toast.makeText(DetectorActivity.this, text2, Toast.LENGTH_SHORT).show();

          //http://stackoverflow.com/a/29777304
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ttsGreater21(text2);
          } else {
            ttsUnder20(text2);
          }
          return;
        }
        if(System.currentTimeMillis()<=btnPressTime+1000){
          Intent it = new Intent(DetectorActivity.this,MapActivity.class);
          it.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
          it.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);


          startActivity(it);
        }
      }
    });
//    buttontext.setOnClickListener(new View.OnClickListener() {
//      @Override
//      public void onClick(View view) {
//        capture();
//      }
//    });



//    tessBaseAPI = new TessBaseAPI();
//    String dir = getFilesDir() + "/tesseract";
//    if(checkLanguageFile(dir+"/tessdata"))
//      tessBaseAPI.init(dir, "eng");




  }


  boolean checkLanguageFile(String dir)
  {
    File file = new File(dir);
    if(!file.exists() && file.mkdirs())
      createFiles(dir);
    else if(file.exists()){
      String filePath = dir + "/eng.traineddata";
      File langDataFile = new File(filePath);
      if(!langDataFile.exists())
        createFiles(dir);
    }
    return true;
  }


  private void createFiles(String dir)
  {
    AssetManager assetMgr = this.getAssets();

    InputStream inputStream = null;
    OutputStream outputStream = null;

    try {
      inputStream = assetMgr.open("eng.traineddata");

      String destFile = dir + "/eng.traineddata";

      outputStream = new FileOutputStream(destFile);

      byte[] buffer = new byte[1024];
      int read;
      while ((read = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, read);
      }
      inputStream.close();
      outputStream.flush();
      outputStream.close();
    }catch (IOException e) {
      e.printStackTrace();
    }
  }



  private void capture()
  {
    surfaceView.capture(new android.hardware.Camera.PictureCallback() {
      @Override
      public void onPictureTaken(byte[] bytes, android.hardware.Camera camera) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;

        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        bitmap = GetRotatedBitmap(bitmap, 90);

        //imageView.setImageBitmap(bitmap);

        buttontext.setEnabled(false);
        buttontext.setText("텍스트 인식중...");
        new AsyncTess().execute(bitmap);

        camera.startPreview();
      }
    });

  }

  public synchronized static Bitmap GetRotatedBitmap(Bitmap bitmap, int degrees) {
    if (degrees != 0 && bitmap != null) {
      Matrix m = new Matrix();
      m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
      try {
        Bitmap b2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
        if (bitmap != b2) {
          bitmap = b2;
        }
      } catch (OutOfMemoryError ex) {
        ex.printStackTrace();
      }
    }
    return bitmap;
  }

  private class AsyncTess extends AsyncTask<Bitmap, Integer, String> {
    @Override
    protected String doInBackground(Bitmap... mRelativeParams) {
      tessBaseAPI.setImage(mRelativeParams[0]);
      return tessBaseAPI.getUTF8Text();
    }

    protected void onPostExecute(String result) {
      textview.setText(result);
      Toast.makeText(DetectorActivity.this, ""+result, Toast.LENGTH_LONG).show();

      buttontext.setEnabled(true);
      buttontext.setText("텍스트 인식");



    }
  }




  // TTS 관련
  @SuppressWarnings("deprecation")
  private void ttsUnder20(String text) {
    HashMap<String, String> map = new HashMap<>();
    map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
    tts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private void ttsGreater21(String text) {
    String utteranceId=this.hashCode() + "";
    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
  }




  @Override
  public void onDestroy() {
    if (tts != null) {
      if(tts.isSpeaking()) {


        tts.stop();
      }
      tts.shutdown();
    }
    super.onDestroy();
  }


}