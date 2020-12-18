package com.example.myarsample;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.collision.Box;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class StartActivity extends AppCompatActivity {

  ArFragment arFragment;
  private ModelRenderable arBagModel, customArBagModel;

  Button deAttachButton;

  boolean isModelLocked = false;

  private AnchorNode staticAnchorBagModel;
  private AnchorNode cameraAnchorModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_start);

    arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.sceneform_ux_fragment);
    deAttachButton = findViewById(R.id.deAttachButton);

    deAttachButton.setOnClickListener(v -> {
      removeAnchorNode(this.staticAnchorBagModel);
      isModelLocked = false;
      this.staticAnchorBagModel = null;
    });

    setUpModel();

    arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
      Anchor anchor = hitResult.createAnchor();
      staticAnchorBagModel = new AnchorNode(anchor);
      staticAnchorBagModel.setParent(arFragment.getArSceneView()
          .getScene());

      removeAnchorNode(this.cameraAnchorModel);
      if (!isModelLocked) {
        createModel(this.staticAnchorBagModel);
        isModelLocked = true;
      }
    });

    arFragment.getArSceneView()
        .getScene()
        .addOnUpdateListener(frameTime -> {
          Frame frame = arFragment.getArSceneView()
              .getArFrame();
          if (frame == null) {
            return;
          }

          if (frame.getCamera()
              .getTrackingState() != TrackingState.TRACKING) {
            return;
          }

          if (isModelLocked) {
            return;
          }

          if (this.cameraAnchorModel != null) {
            removeAnchorNode(this.cameraAnchorModel); // Remove previous 3D model
          }

          // Place the anchor 1m in front of the camera.
          Session session = arFragment.getArSceneView()
              .getSession();

          Anchor newMarkAnchor = session.createAnchor(frame.getCamera()
              .getPose()
              .compose(Pose.makeTranslation(0, 0, -1f)) //This will place the anchor 1M in front of the camera
              .extractTranslation());

          AnchorNode addedAnchorNode = new AnchorNode(newMarkAnchor);
          addedAnchorNode.setRenderable(arBagModel);
          addedAnchorNode.setParent(arFragment.getArSceneView()
              .getScene());

          this.cameraAnchorModel = addedAnchorNode;
        });
  }

  private void removeAnchorNode(AnchorNode nodeToRemove) {
    //Remove an anchor node
    if (nodeToRemove != null) {
      arFragment.getArSceneView()
          .getScene()
          .removeChild(nodeToRemove);
      nodeToRemove.getAnchor()
          .detach();
      nodeToRemove.setParent(null);
    }
  }

  private void setUpModel() {
    // Create 3D model from .SFB file stored in raw directory
    ModelRenderable.builder()
        .setSource(this, R.raw.ar_bag_model)
        .build()
        .thenAccept(modelRenderable -> {
          arBagModel = modelRenderable;
          arBagModel.setShadowReceiver(false);
          arBagModel.setShadowReceiver(false);
        })
        .exceptionally(throwable -> {
          Toast.makeText(this, "Unable to render model One", Toast.LENGTH_LONG)
              .show();
          return null;
        });

    // Create 3D model (cuboid) programmatically
    MaterialFactory.makeTransparentWithColor(this, new com.google.ar.sceneform.rendering.Color(248, 248, 248, 0f))
        .thenAccept(material -> {
          customArBagModel = ShapeFactory.makeCube(new Vector3(0.45f, 0.55f, 0.25f), new Vector3(0.0f, 0.15f, 0.0f),
              material); // 0.45f, 0.55f, 0.25f cm in a real world
          customArBagModel.setShadowCaster(false);
          customArBagModel.setShadowReceiver(false);
        });
  }

  private void createModel(AnchorNode anchorNode) {
    arBagModel.setShadowReceiver(false);
    arBagModel.setShadowCaster(false);
    TransformableNode cartoonPersonNodeThree = new TransformableNode(arFragment.getTransformationSystem());
    cartoonPersonNodeThree.getScaleController()
        .setEnabled(false); // Disable scaling
    cartoonPersonNodeThree.getTranslationController()
        .setEnabled(false); // Disable moving
    cartoonPersonNodeThree.setParent(anchorNode);
    cartoonPersonNodeThree.setRenderable(arBagModel);
    cartoonPersonNodeThree.select();

    calculateAndDisplayDimensions(cartoonPersonNodeThree);
  }

  private void calculateAndDisplayDimensions(TransformableNode transformableNode) {
    Box box = (Box) transformableNode.getRenderable()
        .getCollisionShape();
    Vector3 size = box.getSize();
    Vector3 transformableNodeScale = transformableNode.getWorldScale();
    Vector3 finalSize = new Vector3((float) (Math.round((size.x * transformableNodeScale.x * 100) * 10) / 10.0),
        (float) (Math.round((size.y * transformableNodeScale.y * 100) * 10) / 10.0),
        (float) (Math.round((size.z * transformableNodeScale.z * 100) * 10) / 10.0));
    String x_cm = String.valueOf(finalSize.x);
    String y_cm = String.valueOf(finalSize.y);
    String z_cm = String.valueOf(finalSize.z);

    System.out.println("X = " + x_cm + " Y = " + y_cm + " Z = " + z_cm);

    System.out.println("Shadow  arBagModel.isShadowCaster() is "
        + arBagModel.isShadowCaster()
        + "Shadow  arBagModel"
        + ".isShadowReceiver() is "
        + arBagModel.isShadowReceiver());
  }
}