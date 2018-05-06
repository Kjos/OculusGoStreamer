package net.kajos;

/**
 * Created by kajos on 15-12-15.
 */
public class LowPassFilter {
    private float prev = 0f;
    public float alpha;
    private boolean empty;

    public LowPassFilter(float alpha) {
        setAlpha(alpha);
        empty = true;
    }

    public LowPassFilter(float alpha, float value) {
        setAlpha(alpha);
        prev = value;
        empty = false;
    }

    public void set(float value) {
        prev = value;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public float get(float value) {
        if (value != value)
            return get();

        if (empty) {
            empty = false;
            prev = value;
            return value;
        } else {
            prev = get(prev, value, alpha);
            return prev;
        }
    }

    public float get() {
        return prev;
    }

    public void empty() {
        empty = true;
    }

    private float get(float prev, float value, float overrideAlpha) {
        return prev + overrideAlpha * (value - prev);
    }

    public float get(float value, float overrideAlpha) {
        if (value != value)
            return get();

        if (empty) {
            empty = false;
            prev = value;
            return value;
        } else {
            prev = get(prev, value, overrideAlpha);
            return prev;
        }
    }

    public boolean lower(float amount, float min) {
        get(prev - amount);
        return capMin(min);
    }

    public boolean raise(float amount, float max) {
        get(prev + amount);
        return capMax(max);
    }

    public boolean capMin(float min) {
        if (prev < min) {
            prev = min;
            return true;
        } else {
            return false;
        }
    }

    public boolean capMax(float max) {
        if (prev > max) {
            prev = max;
            return true;
        } else {
            return false;
        }
    }
}
