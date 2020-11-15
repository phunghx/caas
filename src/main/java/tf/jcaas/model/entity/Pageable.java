package tf.jcaas.model.entity;

import java.util.List;

public class Pageable<T> {
    private long total;
    private List<T> data;

    public Pageable(long total, List<T> data) {
        this.total = total;
        this.data = data;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
