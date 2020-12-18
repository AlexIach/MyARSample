package com.example.myarsample

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Material
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.rendering.Texture
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_ar.sceneform_fragment


/**
 * Activity contatining Sceneform fragment, renders shape on command form
 * the previous screen
 */
class ARActivity : AppCompatActivity() {

  lateinit var fragment: ArFragment

  lateinit var anchorNode: AnchorNode

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_ar)

    fragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment

    fragment.arSceneView.scene.addOnUpdateListener {
      Scene.OnUpdateListener {
        // Let the fragment update its state first.
        fragment.onUpdate(it)

        makeCube(null, Color.BLACK)

        // If there is no frame then don't process anything.
        if (fragment.arSceneView.arFrame != null && fragment.arSceneView.arFrame?.camera?.trackingState == TrackingState.TRACKING) {
          if (this.anchorNode == null) {
            val session: Session? = fragment.arSceneView.session
            val pos = floatArrayOf(0f, 0f, -1f)
            val rotation = floatArrayOf(0f, 0f, 0f, 1f)
            val anchor: Anchor? = session?.createAnchor(Pose(pos, rotation))
            anchorNode = AnchorNode(anchor)
            val material: Material = MaterialFactory.makeTransparentWithColor(this, com.google.ar.sceneform.rendering.Color(Color.BLUE)).get()
            anchorNode.renderable = ShapeFactory.makeCube(Vector3(0.2f, 0.2f, 0.2f), Vector3(0.0f, 0.15f, 0.0f), material)
            anchorNode.setParent(fragment.arSceneView.scene)
          }
        }
      }
    }

    fragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->

      when (intent.extras?.getInt("order")) {

        1 -> makeSphere(hitResult, Color.BLUE)

        2 -> makeCylinder(hitResult, Color.GREEN)

        3 -> makeCube(hitResult, Color.RED)

        4 -> makeTextureSphere(hitResult, R.drawable.sun)
      }

    }
  }


  /**
   * Constructs sphere of radius 1f and at position 0.0f, 0.15f, 0.0f and with TEXTURE
   * @param hitResult - If the hit result is a plane
   * @param res - Image res for texture, here [R.drawable.sun]
   */
  private fun makeTextureSphere(hitResult: HitResult, res: Int) {
    Texture.builder().setSource(BitmapFactory.decodeResource(resources, res))
        .build()
        .thenAccept {
          MaterialFactory.makeOpaqueWithTexture(this, it)
              .thenAccept { material ->
                addNodeToScene(fragment, hitResult.createAnchor(),
                    ShapeFactory.makeSphere(0.1f, Vector3(0.0f, 0.15f, 0.0f), material))

              }
        }
  }

  /**
   * Constructs sphere of radius 1f and at position 0.0f, 0.15f, 0.0f on the plane
   * @param hitResult - If the hit result is a plane
   * @param res - Color
   */
  private fun makeSphere(hitResult: HitResult, color: Int) {
    MaterialFactory.makeOpaqueWithColor(this,
        com.google.ar.sceneform.rendering.Color(color))
        .thenAccept { material ->
          addNodeToScene(fragment, hitResult.createAnchor(),
              ShapeFactory.makeSphere(0.1f, Vector3(0.0f, 0.15f, 0.0f), material))

        }
  }

  /**
   * Constructs cylinder of radius 1f and at position 0.0f, 0.15f, 0.0f on the plane
   * Need to mention height for the cylinder
   * @param hitResult - If the hit result is a plane
   * @param res - Color
   */
  private fun makeCylinder(hitResult: HitResult, color: Int) {
    MaterialFactory.makeOpaqueWithColor(this,
        com.google.ar.sceneform.rendering.Color(color))
        .thenAccept { material ->
          addNodeToScene(fragment, hitResult.createAnchor(),
              ShapeFactory.makeCylinder(0.1f, 0.3f, Vector3(0.0f, 0.15f, 0.0f), material))

        }
  }

  /**
   * Constructs cube of radius 1f and at position 0.0f, 0.15f, 0.0f on the plane
   * Here Vector3 takes up the size - 0.2f, 0.2f, 0.2f
   * @param hitResult - If the hit result is a plane
   * @param res - Color
   */
  private fun makeCube(hitResult: HitResult?, color: Int) {
    MaterialFactory.makeOpaqueWithColor(this,
        com.google.ar.sceneform.rendering.Color(color))
        .thenAccept { material ->
          val pos = floatArrayOf(0f, 0f, -1f)
          val rotation = floatArrayOf(0f, 0f, 0f, 1f)
          val anchor: Anchor? = fragment.arSceneView.session?.createAnchor(Pose(pos, rotation))
          addNodeToScene(fragment, anchor!!/*hitResult.createAnchor()*/,
              ShapeFactory.makeCube(Vector3(0.2f, 0.2f, 0.2f), Vector3(0.0f, 0.15f, 0.0f), material))

        }
  }

  private fun makeDefaultCube(anchor: Anchor, color: Int) {
    MaterialFactory.makeOpaqueWithColor(this,
        com.google.ar.sceneform.rendering.Color(color))
        .thenAccept { material ->
          addNodeToScene(fragment, anchor, ShapeFactory.makeCube(Vector3(0.2f, 0.2f, 0.2f), Vector3(0.0f, 0.15f, 0.0f), material))

        }
  }


  /**
   * Adds node to the scene and the object.
   * @param fragment - sceneform fragment
   * @param anchor - created anchor at the tapped position
   * @param modelObject - rendered object
   */
  private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, modelObject: ModelRenderable) {

    val anchorNode = AnchorNode(anchor)

    TransformableNode(fragment.transformationSystem).apply {
      renderable = modelObject
      setParent(anchorNode)
      select()
    }

    fragment.arSceneView.scene.addChild(anchorNode)
  }
}