package ru.serjik.hexshaders.renderer.legacy

import android.content.res.AssetManager
import android.opengl.GLES20
import ru.serjik.engine.gl.ShaderProgram
import ru.serjik.engine.gl.Texture
import ru.serjik.utils.AssetsUtils
import java.nio.Buffer
import java.nio.FloatBuffer
import kotlin.math.abs

/**
 * Shader program for the legacy (single-pass) hex rendering mode.
 * Renders all hexagons as point sprites in one draw call.
 */
class LegacyShaderHex(
    assetManager: AssetManager,
    shaderSource: String,
    textureNames: List<String>,
    private val timeScale: Float
) : ShaderProgram(shaderSource) {

    private var globalTime = 0.0f

    var uniformSize: Int
    var uniformTexture: Int
    var attribPos: Int

    private val uniformResolution: Int
    private val uniformGlobalTime: Int
    private val uniformTimeDelta: Int
    private val uniformFrame: Int
    private val uniformChannels = intArrayOf(-1, -1, -1, -1)
    private var frameCount = 0

    init {
        // Load additional channel textures
        for (i in textureNames.indices) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1 + i)
            Texture(AssetsUtils.readBitmap("textures/${textureNames[i]}", assetManager), true)
            Texture.setFilter(GLES20.GL_LINEAR, GLES20.GL_LINEAR)
            Texture.setWrap(GLES20.GL_REPEAT, GLES20.GL_REPEAT)
            uniformChannels[i] = getUniformLocation("iChannel$i")
        }

        // Load hex point sprite texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        Texture(AssetsUtils.readBitmap("textures/hex.png", assetManager), true)
        Texture.setFilter(GLES20.GL_LINEAR, GLES20.GL_LINEAR)
        Texture.setWrap(GLES20.GL_CLAMP_TO_EDGE, GLES20.GL_CLAMP_TO_EDGE)

        uniformResolution = getUniformLocation("iResolution")
        uniformGlobalTime = getUniformLocation("iGlobalTime")
        uniformTimeDelta = getUniformLocation("iTimeDelta")
        uniformFrame = getUniformLocation("iFrame")
        uniformSize = getUniformLocation("u_size")
        uniformTexture = getUniformLocation("u_texture")
        attribPos = getAttribLocation("a_pos")
    }

    /** Draws all hex points in a single pass. */
    fun draw(
        deltaSeconds: Float,
        screenWidth: Float,
        screenHeight: Float,
        pointSize: Float,
        positions: FloatBuffer,
        count: Int
    ) {
        use()
        bindPositions(positions)
        val scaledDelta = timeScale * deltaSeconds
        frameCount++
        globalTime += scaledDelta
        if (abs(globalTime) > abs(60000.0f * timeScale) && abs(timeScale) > 0.01f) {
            globalTime = 0.0f
        }
        GLES20.glUniform3f(uniformResolution, screenWidth, screenHeight, screenWidth / screenHeight)
        GLES20.glUniform1f(uniformGlobalTime, globalTime)
        GLES20.glUniform1f(uniformTimeDelta, scaledDelta)
        GLES20.glUniform1i(uniformFrame, frameCount)
        GLES20.glUniform1i(uniformChannels[0], 1)
        GLES20.glUniform1i(uniformChannels[1], 2)
        GLES20.glUniform1i(uniformChannels[2], 3)
        GLES20.glUniform1i(uniformChannels[3], 4)
        GLES20.glUniform1f(uniformSize, pointSize)
        GLES20.glUniform1i(uniformTexture, 0)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE)
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, count)
    }

    /** Binds the hex position buffer to the vertex attribute. */
    fun bindPositions(positions: FloatBuffer) {
        positions.position(0)
        GLES20.glVertexAttribPointer(attribPos, 2, GLES20.GL_FLOAT, false, 8, positions as Buffer)
        GLES20.glEnableVertexAttribArray(attribPos)
    }
}
