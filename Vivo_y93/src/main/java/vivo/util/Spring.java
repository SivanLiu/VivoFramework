package vivo.util;

public class Spring {
    private static int ID = 0;
    private static final double MAX_DELTA_TIME_SEC = 0.064d;
    private static final double SOLVER_TIMESTEP_SEC = 0.001d;
    private final PhysicsState mCurrentState = new PhysicsState();
    private double mDisplacementFromRestThreshold = 0.005d;
    private double mEndValue;
    private final String mId;
    private boolean mOvershootClampingEnabled;
    private final PhysicsState mPreviousState = new PhysicsState();
    private double mRestSpeedThreshold = 0.005d;
    private SpringConfig mSpringConfig;
    private double mStartValue;
    private final PhysicsState mTempState = new PhysicsState();
    private double mTimeAccumulator = 0.0d;
    private boolean mWasAtRest = true;

    private static class PhysicsState {
        double position;
        double velocity;

        /* synthetic */ PhysicsState(PhysicsState -this0) {
            this();
        }

        private PhysicsState() {
        }
    }

    public Spring() {
        StringBuilder append = new StringBuilder().append("spring:");
        int i = ID;
        ID = i + 1;
        this.mId = append.append(i).toString();
        setSpringConfig(SpringConfig.defaultConfig);
    }

    public void destroy() {
    }

    public String getId() {
        return this.mId;
    }

    public Spring setSpringConfig(SpringConfig springConfig) {
        if (springConfig == null) {
            throw new IllegalArgumentException("springConfig is required");
        }
        this.mSpringConfig = springConfig;
        return this;
    }

    public SpringConfig getSpringConfig() {
        return this.mSpringConfig;
    }

    public Spring setCurrentValue(double currentValue) {
        return setCurrentValue(currentValue, true);
    }

    public Spring setCurrentValue(double currentValue, boolean setAtRest) {
        this.mStartValue = currentValue;
        this.mCurrentState.position = currentValue;
        if (setAtRest) {
            setAtRest();
        }
        return this;
    }

    public double getStartValue() {
        return this.mStartValue;
    }

    public double getCurrentValue() {
        return this.mCurrentState.position;
    }

    public double getCurrentDisplacementDistance() {
        return getDisplacementDistanceForState(this.mCurrentState);
    }

    private double getDisplacementDistanceForState(PhysicsState state) {
        return Math.abs(this.mEndValue - state.position);
    }

    public Spring setEndValue(double endValue) {
        if (this.mEndValue == endValue && isAtRest()) {
            return this;
        }
        this.mStartValue = getCurrentValue();
        this.mEndValue = endValue;
        return this;
    }

    public double getEndValue() {
        return this.mEndValue;
    }

    public Spring setVelocity(double velocity) {
        if (velocity == this.mCurrentState.velocity) {
            return this;
        }
        this.mCurrentState.velocity = velocity;
        return this;
    }

    public double getVelocity() {
        return this.mCurrentState.velocity;
    }

    public Spring setRestSpeedThreshold(double restSpeedThreshold) {
        this.mRestSpeedThreshold = restSpeedThreshold;
        return this;
    }

    public double getRestSpeedThreshold() {
        return this.mRestSpeedThreshold;
    }

    public Spring setRestDisplacementThreshold(double displacementFromRestThreshold) {
        this.mDisplacementFromRestThreshold = displacementFromRestThreshold;
        return this;
    }

    public double getRestDisplacementThreshold() {
        return this.mDisplacementFromRestThreshold;
    }

    public Spring setOvershootClampingEnabled(boolean overshootClampingEnabled) {
        this.mOvershootClampingEnabled = overshootClampingEnabled;
        return this;
    }

    public boolean isOvershootClampingEnabled() {
        return this.mOvershootClampingEnabled;
    }

    public boolean isOvershooting() {
        if (this.mSpringConfig.tension <= 0.0d) {
            return false;
        }
        if (this.mStartValue < this.mEndValue && getCurrentValue() > this.mEndValue) {
            return true;
        }
        if (this.mStartValue <= this.mEndValue || getCurrentValue() >= this.mEndValue) {
            return false;
        }
        return true;
    }

    public void advance(double realDeltaTime) {
        boolean isAtRest = isAtRest();
        if (!isAtRest || !this.mWasAtRest) {
            double adjustedDeltaTime = realDeltaTime;
            if (realDeltaTime > MAX_DELTA_TIME_SEC) {
                adjustedDeltaTime = MAX_DELTA_TIME_SEC;
            }
            this.mTimeAccumulator += adjustedDeltaTime;
            double tension = this.mSpringConfig.tension;
            double friction = this.mSpringConfig.friction;
            double position = this.mCurrentState.position;
            double velocity = this.mCurrentState.velocity;
            double tempPosition = this.mTempState.position;
            double tempVelocity = this.mTempState.velocity;
            while (this.mTimeAccumulator >= SOLVER_TIMESTEP_SEC) {
                this.mTimeAccumulator -= SOLVER_TIMESTEP_SEC;
                if (this.mTimeAccumulator < SOLVER_TIMESTEP_SEC) {
                    this.mPreviousState.position = position;
                    this.mPreviousState.velocity = velocity;
                }
                double aVelocity = velocity;
                double aAcceleration = ((this.mEndValue - tempPosition) * tension) - (friction * velocity);
                tempVelocity = velocity + ((SOLVER_TIMESTEP_SEC * aAcceleration) * 0.5d);
                double bVelocity = tempVelocity;
                double bAcceleration = ((this.mEndValue - (position + ((SOLVER_TIMESTEP_SEC * aVelocity) * 0.5d))) * tension) - (friction * tempVelocity);
                tempPosition = position + ((SOLVER_TIMESTEP_SEC * tempVelocity) * 0.5d);
                tempVelocity = velocity + ((SOLVER_TIMESTEP_SEC * bAcceleration) * 0.5d);
                double cVelocity = tempVelocity;
                double cAcceleration = ((this.mEndValue - tempPosition) * tension) - (friction * tempVelocity);
                tempPosition = position + (SOLVER_TIMESTEP_SEC * tempVelocity);
                tempVelocity = velocity + (SOLVER_TIMESTEP_SEC * cAcceleration);
                double dVelocity = tempVelocity;
                position += SOLVER_TIMESTEP_SEC * (0.16666666666666666d * ((((bVelocity + cVelocity) * 2.0d) + aVelocity) + tempVelocity));
                velocity += SOLVER_TIMESTEP_SEC * (0.16666666666666666d * ((((bAcceleration + cAcceleration) * 2.0d) + aAcceleration) + (((this.mEndValue - tempPosition) * tension) - (friction * tempVelocity))));
            }
            this.mTempState.position = tempPosition;
            this.mTempState.velocity = tempVelocity;
            this.mCurrentState.position = position;
            this.mCurrentState.velocity = velocity;
            if (this.mTimeAccumulator > 0.0d) {
                interpolate(this.mTimeAccumulator / SOLVER_TIMESTEP_SEC);
            }
            if (isAtRest() || (this.mOvershootClampingEnabled && isOvershooting())) {
                if (tension > 0.0d) {
                    this.mStartValue = this.mEndValue;
                    this.mCurrentState.position = this.mEndValue;
                } else {
                    this.mEndValue = this.mCurrentState.position;
                    this.mStartValue = this.mEndValue;
                }
                setVelocity(0.0d);
                isAtRest = true;
            }
            if (this.mWasAtRest) {
                this.mWasAtRest = false;
            }
            if (isAtRest) {
                this.mWasAtRest = true;
            }
        }
    }

    public boolean systemShouldAdvance() {
        return isAtRest() ? wasAtRest() ^ 1 : true;
    }

    public boolean wasAtRest() {
        return this.mWasAtRest;
    }

    public boolean isAtRest() {
        if (Math.abs(this.mCurrentState.velocity) <= this.mRestSpeedThreshold) {
            return getDisplacementDistanceForState(this.mCurrentState) <= this.mDisplacementFromRestThreshold || this.mSpringConfig.tension == 0.0d;
        } else {
            return false;
        }
    }

    public Spring setAtRest() {
        this.mEndValue = this.mCurrentState.position;
        this.mTempState.position = this.mCurrentState.position;
        this.mCurrentState.velocity = 0.0d;
        return this;
    }

    private void interpolate(double alpha) {
        this.mCurrentState.position = (this.mCurrentState.position * alpha) + (this.mPreviousState.position * (1.0d - alpha));
        this.mCurrentState.velocity = (this.mCurrentState.velocity * alpha) + (this.mPreviousState.velocity * (1.0d - alpha));
    }

    public boolean currentValueIsApproximately(double value) {
        return Math.abs(getCurrentValue() - value) <= getRestDisplacementThreshold();
    }
}
