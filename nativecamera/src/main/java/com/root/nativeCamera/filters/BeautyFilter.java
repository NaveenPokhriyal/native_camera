package com.root.nativeCamera.filters;

import android.opengl.GLES20;

import androidx.annotation.NonNull;

import com.otaliastudios.cameraview.filter.BaseFilter;
import com.otaliastudios.cameraview.filter.OneParameterFilter;

import java.nio.FloatBuffer;

public class BeautyFilter extends BaseFilter implements OneParameterFilter {

    private static final String FRAGMENT_SHADER = "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying mediump vec2 vTextureCoord;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "uniform vec2 singleStepOffset;\n" +
            "uniform mediump float params;\n" +
            "\n" +
            "const highp vec3 W = vec3(0.299, 0.587, 0.114);\n" +
            "vec2 blurCoordinates[20];\n" +
            "\n" +
            "float hardLight(float color)\n" +
            "{\n" +
            "    if (color <= 0.5)\n" +
            "    color = color * color * 2.0;\n" +
            "    else\n" +
            "    color = 1.0 - ((1.0 - color)*(1.0 - color) * 2.0);\n" +
            "    return color;\n" +
            "}\n" +
            "\n" +
            "void main(){\n" +
            "\n" +
            "    vec3 centralColor = texture2D(sTexture, vTextureCoord).rgb;\n" +
            "    blurCoordinates[0] = vTextureCoord.xy + singleStepOffset * vec2(0.0, -10.0);\n" +
            "    blurCoordinates[1] = vTextureCoord.xy + singleStepOffset * vec2(0.0, 10.0);\n" +
            "    blurCoordinates[2] = vTextureCoord.xy + singleStepOffset * vec2(-10.0, 0.0);\n" +
            "    blurCoordinates[3] = vTextureCoord.xy + singleStepOffset * vec2(10.0, 0.0);\n" +
            "    blurCoordinates[4] = vTextureCoord.xy + singleStepOffset * vec2(5.0, -8.0);\n" +
            "    blurCoordinates[5] = vTextureCoord.xy + singleStepOffset * vec2(5.0, 8.0);\n" +
            "    blurCoordinates[6] = vTextureCoord.xy + singleStepOffset * vec2(-5.0, 8.0);\n" +
            "    blurCoordinates[7] = vTextureCoord.xy + singleStepOffset * vec2(-5.0, -8.0);\n" +
            "    blurCoordinates[8] = vTextureCoord.xy + singleStepOffset * vec2(8.0, -5.0);\n" +
            "    blurCoordinates[9] = vTextureCoord.xy + singleStepOffset * vec2(8.0, 5.0);\n" +
            "    blurCoordinates[10] = vTextureCoord.xy + singleStepOffset * vec2(-8.0, 5.0);\n" +
            "    blurCoordinates[11] = vTextureCoord.xy + singleStepOffset * vec2(-8.0, -5.0);\n" +
            "    blurCoordinates[12] = vTextureCoord.xy + singleStepOffset * vec2(0.0, -6.0);\n" +
            "    blurCoordinates[13] = vTextureCoord.xy + singleStepOffset * vec2(0.0, 6.0);\n" +
            "    blurCoordinates[14] = vTextureCoord.xy + singleStepOffset * vec2(6.0, 0.0);\n" +
            "    blurCoordinates[15] = vTextureCoord.xy + singleStepOffset * vec2(-6.0, 0.0);\n" +
            "    blurCoordinates[16] = vTextureCoord.xy + singleStepOffset * vec2(-4.0, -4.0);\n" +
            "    blurCoordinates[17] = vTextureCoord.xy + singleStepOffset * vec2(-4.0, 4.0);\n" +
            "    blurCoordinates[18] = vTextureCoord.xy + singleStepOffset * vec2(4.0, -4.0);\n" +
            "    blurCoordinates[19] = vTextureCoord.xy + singleStepOffset * vec2(4.0, 4.0);\n" +
            "\n" +
            "    float sampleColor = centralColor.g * 20.0;\n" +
            "    sampleColor += texture2D(sTexture, blurCoordinates[0]).g;\n" +
            "    sampleColor += texture2D(sTexture, blurCoordinates[1]).g;\n" +
            "    sampleColor += texture2D(sTexture, blurCoordinates[2]).g;\n" +
            "    sampleColor += texture2D(sTexture, blurCoordinates[3]).g;\n" +
            "    sampleColor += texture2D(sTexture, blurCoordinates[4]).g;\n" +
            "    sampleColor += texture2D(sTexture, blurCoordinates[5]).g;\n" +
            "    sampleColor += texture2D(sTexture, blurCoordinates[6]).g;\n" +
            "    sampleColor += texture2D(sTexture, blurCoordinates[7]).g;\n" +
            "    sampleColor += texture2D(sTexture, blurCoordinates[8]).g;\n" +
            "    sampleColor += texture2D(sTexture, blurCoordinates[9]).g;\n" +
            "    sampleColor += texture2D(sTexture, blurCoordinates[10]).g;\n" +
            "    sampleColor += texture2D(sTexture, blurCoordinates[11]).g;\n" +
            "    sampleColor += texture2D(sTexture, blurCoordinates[12]).g * 2.0;\n" +
            "    sampleColor += texture2D(sTexture, blurCoordinates[13]).g * 2.0;\n" +
            "    sampleColor += texture2D(sTexture, blurCoordinates[14]).g * 2.0;\n" +
            "    sampleColor += texture2D(sTexture, blurCoordinates[15]).g * 2.0;\n" +
            "    sampleColor += texture2D(sTexture, blurCoordinates[16]).g * 2.0;\n" +
            "    sampleColor += texture2D(sTexture, blurCoordinates[17]).g * 2.0;\n" +
            "    sampleColor += texture2D(sTexture, blurCoordinates[18]).g * 2.0;\n" +
            "    sampleColor += texture2D(sTexture, blurCoordinates[19]).g * 2.0;\n" +
            "\n" +
            "    sampleColor = sampleColor / 48.0;\n" +
            "\n" +
            "    float highPass = centralColor.g - sampleColor + 0.5;\n" +
            "\n" +
            "    for (int i = 0; i < 5;i++)\n" +
            "    {\n" +
            "        highPass = hardLight(highPass);\n" +
            "    }\n" +
            "    float luminance = dot(centralColor, W);\n" +
            "\n" +
            "    float alpha = pow(luminance, params);\n" +
            "\n" +
            "    vec3 smoothColor = centralColor + (centralColor-vec3(highPass))*alpha*0.1;\n" +
            "\n" +
            "    gl_FragColor = vec4(mix(smoothColor.rgb, max(smoothColor, centralColor), alpha), 1.0);\n" +
            "}";

    private float beautyLevel = 3.0f;
    private int width = 1;
    private int height = 1;
    private int mSingleStepOffsetLocation;
    private int mParamsLocation;

    public BeautyFilter() {
    }

    @NonNull
    @Override
    public String getFragmentShader() {
        return FRAGMENT_SHADER;
    }

    @Override
    public void onCreate(int programHandle) {
        super.onCreate(programHandle);
        mSingleStepOffsetLocation = GLES20.glGetUniformLocation(programHandle, "singleStepOffset");
//        Egloo.checkGlProgramLocation(mSingleStepOffsetLocation, "singleStepOffset");
        mParamsLocation = GLES20.glGetUniformLocation(programHandle, "params");
//        Egloo.checkGlProgramLocation(mParamsLocation, "params");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSingleStepOffsetLocation = -1;
        mParamsLocation = -1;
    }

    public void setBeautyLevel(float beautyLevel) {
        this.beautyLevel = beautyLevel;
    }

    public float getBeautyLevel() {
        return beautyLevel;
    }

    @Override
    protected void onPreDraw(long timestampUs, @NonNull float[] transformMatrix) {
        super.onPreDraw(timestampUs, transformMatrix);
        GLES20.glUniform1f(mParamsLocation, beautyLevel);
//        Egloo.checkGlError("glUniform1f");
        GLES20.glUniform2fv(mSingleStepOffsetLocation, 1, FloatBuffer.wrap(new float[]{3.0f / width, 3.0f / height}));
//        Egloo.checkGlError("glUniform2fv");
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        this.height = height;
        this.width = width;
    }

    @Override
    public void setParameter1(float value) {
        setBeautyLevel(value);
    }

    @Override
    public float getParameter1() {
        return getBeautyLevel();
    }


}
