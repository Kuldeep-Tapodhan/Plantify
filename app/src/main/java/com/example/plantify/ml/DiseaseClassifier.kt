package com.example.plantify.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException

// A data class to hold the result of the classification
data class ClassificationResult(
    val diseaseName: String,
    val confidence: Float
)

class DiseaseClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()

    private val modelInputWidth = 224
    private val modelInputHeight = 224

    fun init() {
        try {
            val model = FileUtil.loadMappedFile(context, "tflite_model.tflite")
            interpreter = Interpreter(model)
            labels = FileUtil.loadLabels(context, "labels.txt")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun classify(bitmap: Bitmap): ClassificationResult {
        if (interpreter == null || labels.isEmpty()) {
            return ClassificationResult("Model not initialized", 0f)
        }

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(modelInputHeight, modelInputWidth, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(127.5f, 127.5f)) // Normalize to [-1, 1]
            .build()

        var tensorImage = TensorImage.fromBitmap(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        val probabilityBuffer = TensorBuffer.createFixedSize(intArrayOf(1, labels.size), org.tensorflow.lite.DataType.FLOAT32)

        interpreter?.run(tensorImage.buffer, probabilityBuffer.buffer.rewind())

        val probabilities = probabilityBuffer.floatArray
        val maxProbabilityIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: -1

        return if (maxProbabilityIndex != -1) {
            ClassificationResult(
                diseaseName = labels[maxProbabilityIndex],
                confidence = probabilities[maxProbabilityIndex]
            )
        } else {
            ClassificationResult("Unknown", 0f)
        }
    }

    fun close() {
        interpreter?.close()
    }
}