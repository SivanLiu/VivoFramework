package com.vivo.services.motion.gesture.engine;

import com.vivo.services.motion.gesture.util.Vector3D;

public class DCBlockFilter3D {
    float a;
    Vector3D lastInput;
    Vector3D lastOutput;

    public DCBlockFilter3D() {
        this.lastInput = Vector3D.ZERO;
        this.lastOutput = Vector3D.ZERO;
        this.a = 0.9f;
    }

    public DCBlockFilter3D(float paramFloat) {
        this.lastInput = Vector3D.ZERO;
        this.lastOutput = Vector3D.ZERO;
        this.a = paramFloat;
    }

    public Vector3D filter(Vector3D paramVector3D) {
        Vector3D localVector3D = new Vector3D(((((this.a + 1.0f) / 2.0f) * paramVector3D.getX()) - (((this.a + 1.0f) / 2.0f) * this.lastInput.getX())) + (this.a * this.lastOutput.getX()), ((((this.a + 1.0f) / 2.0f) * paramVector3D.getY()) - (((this.a + 1.0f) / 2.0f) * this.lastInput.getY())) + (this.a * this.lastOutput.getY()), ((((this.a + 1.0f) / 2.0f) * paramVector3D.getZ()) - (((this.a + 1.0f) / 2.0f) * this.lastInput.getZ())) + (this.a * this.lastOutput.getZ()));
        this.lastInput = paramVector3D;
        this.lastOutput = localVector3D;
        return localVector3D;
    }
}
