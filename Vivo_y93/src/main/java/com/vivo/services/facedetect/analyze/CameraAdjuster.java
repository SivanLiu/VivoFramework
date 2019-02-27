package com.vivo.services.facedetect.analyze;

import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.Parameters;
import com.vivo.framework.facedetect.AdjusterParams;
import java.util.ArrayList;

public class CameraAdjuster {
    final DarkState DARK_STATE = new DarkState(this);
    final HDRState HDR_STATE = new HDRState(this);
    final NormalState NORMAL_STATE = new NormalState(this);
    final OverLightState OVERLIGHT_STATE = new OverLightState(this);
    private YUVImageAnalysor mAnalysor = new YUVImageAnalysor(this.mParams);
    private Camera mCamera;
    private AdjusterParams mParams = new AdjusterParams();
    private AnalyzeResult mResult;
    private State mState = this.NORMAL_STATE;

    public static abstract class State {
        private CameraAdjuster adjuster;

        public abstract void adjustExposureAndMeteringArea(AnalyzeResult analyzeResult);

        public State(CameraAdjuster adjuster) {
            this.adjuster = adjuster;
        }

        public CameraAdjuster getAdjuster() {
            return this.adjuster;
        }
    }

    public static class DarkState extends State {
        public DarkState(CameraAdjuster adjuster) {
            super(adjuster);
        }

        public void adjustExposureAndMeteringArea(AnalyzeResult result) {
            switch (result.mType) {
                case 0:
                    getAdjuster().setState(getAdjuster().NORMAL_STATE);
                    return;
                case 1:
                    getAdjuster().setExposure(40);
                    getAdjuster().setState(getAdjuster().DARK_STATE);
                    return;
                case 2:
                    getAdjuster().setMeteringArea(result.darkestArea);
                    getAdjuster().setState(getAdjuster().HDR_STATE);
                    return;
                case 3:
                    getAdjuster().setState(getAdjuster().OVERLIGHT_STATE);
                    return;
                default:
                    return;
            }
        }
    }

    public static class HDRState extends State {
        public HDRState(CameraAdjuster adjuster) {
            super(adjuster);
        }

        public void adjustExposureAndMeteringArea(AnalyzeResult result) {
            switch (result.mType) {
                case 0:
                    getAdjuster().setState(getAdjuster().NORMAL_STATE);
                    return;
                case 1:
                    getAdjuster().setExposure(40);
                    getAdjuster().setState(getAdjuster().DARK_STATE);
                    return;
                case 2:
                    getAdjuster().addExposure(getAdjuster().getParams().smallMediumExp);
                    getAdjuster().setMeteringArea(result.darkestArea);
                    getAdjuster().setState(getAdjuster().HDR_STATE);
                    return;
                case 3:
                    getAdjuster().addExposure(-getAdjuster().getParams().miniExp);
                    getAdjuster().setState(getAdjuster().OVERLIGHT_STATE);
                    return;
                default:
                    return;
            }
        }
    }

    public static class NormalState extends State {
        public NormalState(CameraAdjuster adjuster) {
            super(adjuster);
        }

        public void adjustExposureAndMeteringArea(AnalyzeResult result) {
            switch (result.mType) {
                case 1:
                    getAdjuster().setExposure(40);
                    getAdjuster().setState(getAdjuster().DARK_STATE);
                    return;
                case 2:
                    getAdjuster().setMeteringArea(result.darkestArea);
                    getAdjuster().setState(getAdjuster().HDR_STATE);
                    return;
                case 3:
                    getAdjuster().setState(getAdjuster().OVERLIGHT_STATE);
                    return;
                default:
                    return;
            }
        }
    }

    public static class OverLightState extends State {
        public OverLightState(CameraAdjuster adjuster) {
            super(adjuster);
        }

        public void adjustExposureAndMeteringArea(AnalyzeResult result) {
            switch (result.mType) {
                case 0:
                    getAdjuster().setState(getAdjuster().NORMAL_STATE);
                    return;
                case 1:
                    getAdjuster().setExposure(40);
                    getAdjuster().setState(getAdjuster().DARK_STATE);
                    return;
                case 2:
                    getAdjuster().setMeteringArea(result.darkestArea);
                    getAdjuster().setState(getAdjuster().HDR_STATE);
                    return;
                case 3:
                    getAdjuster().addExposure(-getAdjuster().getParams().miniExp);
                    getAdjuster().setState(getAdjuster().OVERLIGHT_STATE);
                    return;
                default:
                    return;
            }
        }
    }

    public void setState(State state) {
        synchronized (this.mState) {
            this.mState = state;
        }
    }

    public void adjustCamera(byte[] data, Camera camera) {
        this.mResult = this.mAnalysor.analyze(new YUVImageData(data, 480, 640));
        this.mCamera = camera;
        synchronized (this.mState) {
            LogUtils.debugLog("analyze result " + this.mResult.toString());
            this.mState.adjustExposureAndMeteringArea(this.mResult);
        }
    }

    public void addExposure(int value) {
        long now = System.currentTimeMillis();
        LogUtils.debugLog("startSetExposure with value" + value);
        if (this.mCamera != null) {
            Parameters parameters = this.mCamera.getParameters();
            if (parameters != null) {
                value += parameters.getExposureCompensation();
                if (value < 0) {
                    value = 0;
                } else {
                    int max = parameters.getMaxExposureCompensation();
                    if (value > max) {
                        value = max;
                    }
                }
                parameters.setExposureCompensation(value);
                this.mCamera.setParameters(parameters);
            }
            LogUtils.debugLog("startSetExposure cost time: " + (System.currentTimeMillis() - now));
        }
    }

    public void setExposure(int value) {
        long now = System.currentTimeMillis();
        LogUtils.debugLog("startSetExposure with value" + value);
        if (this.mCamera != null) {
            Parameters parameters = this.mCamera.getParameters();
            if (parameters != null) {
                if (value < 0) {
                    value = 0;
                } else {
                    int max = parameters.getMaxExposureCompensation();
                    if (value > max) {
                        value = max;
                    }
                }
                parameters.setExposureCompensation(value);
                this.mCamera.setParameters(parameters);
            }
            LogUtils.debugLog("startSetExposure cost time: " + (System.currentTimeMillis() - now));
        }
    }

    public void setMeteringArea(Rect rect) {
        long now = System.currentTimeMillis();
        if (this.mCamera != null) {
            Parameters parameters = this.mCamera.getParameters();
            if (parameters != null) {
                if (rect != null) {
                    Area area = new Area(rect, 1000);
                    ArrayList<Area> areas = new ArrayList();
                    areas.add(area);
                    parameters.setMeteringAreas(areas);
                } else {
                    parameters.setMeteringAreas(null);
                }
                this.mCamera.setParameters(parameters);
            }
            LogUtils.debugLog("setMeteringArea cost time: " + (System.currentTimeMillis() - now));
        }
    }

    public AnalyzeResult getResult() {
        return this.mResult;
    }

    public void reset() {
        setState(this.NORMAL_STATE);
    }

    public AdjusterParams getParams() {
        return this.mParams;
    }

    public void setParams(AdjusterParams params) {
        if (params != null) {
            this.mParams = params;
            this.mAnalysor.setParameters(this.mParams);
        }
    }
}
