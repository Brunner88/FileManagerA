package utility;

public class ElementCount {
    private String element;
    private int total;

    public ElementCount(String element, int count) {
        this.element = element;
        this.total = count;
    }

    public void setElement(String element) {
        this.element = element;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getElement() {
        return element;
    }

    public int getTotal() {
        return total;
    }

    @Override
    public String toString() {
        return "ElementCount{" +
                "element='" + element + '\'' +
                ", total=" + total +
                '}';
    }
}