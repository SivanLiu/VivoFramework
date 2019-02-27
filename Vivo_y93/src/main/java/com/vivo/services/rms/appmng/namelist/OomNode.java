package com.vivo.services.rms.appmng.namelist;

public class OomNode {
    public int adj;
    public int procState;
    public int schedGroup;

    public OomNode(int adj, int state, int sched) {
        this.adj = adj;
        this.procState = state;
        this.schedGroup = sched;
    }
}
