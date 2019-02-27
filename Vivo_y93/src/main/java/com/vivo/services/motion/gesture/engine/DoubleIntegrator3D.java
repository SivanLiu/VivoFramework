package com.vivo.services.motion.gesture.engine;

import com.vivo.services.motion.gesture.util.Vector3D;

public class DoubleIntegrator3D {
    float alpha;
    DCBlockFilter3D filter1 = null;
    DCBlockFilter3D filter2 = null;
    DCBlockFilter3D filter3 = null;
    float period;
    Vector3D position = Vector3D.ZERO;
    Vector3D raw_pos = Vector3D.ZERO;
    Vector3D raw_vel = Vector3D.ZERO;
    Vector3D velocity = Vector3D.ZERO;

    public DoubleIntegrator3D(float paramFloat1, float paramFloat2) {
        this.alpha = 1.0f - paramFloat1;
        this.period = paramFloat2;
        reset();
    }

    public Vector3D process(Vector3D paramVector3D) {
        return process(paramVector3D, this.period);
    }

    public Vector3D process(Vector3D paramVector3D, float paramFloat) {
        this.raw_vel = this.raw_vel.add(paramFloat, this.filter1.filter(paramVector3D));
        this.velocity = this.filter2.filter(this.raw_vel);
        this.raw_pos = this.raw_pos.add(paramFloat, this.velocity);
        this.position = this.raw_pos;
        return this.position;
    }

    public void reset() {
        this.velocity = Vector3D.ZERO;
        this.position = Vector3D.ZERO;
        this.raw_vel = Vector3D.ZERO;
        this.raw_pos = Vector3D.ZERO;
        this.filter1 = new DCBlockFilter3D(this.alpha);
        this.filter2 = new DCBlockFilter3D(this.alpha);
        this.filter3 = new DCBlockFilter3D(this.alpha);
    }

    public void resetPosition() {
        this.position = Vector3D.ZERO;
        this.raw_pos = Vector3D.ZERO;
    }
}
