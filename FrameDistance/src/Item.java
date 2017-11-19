public class Item {

    private int expire;

    public Item(int expire) {
        this.expire = expire;
    }

    public void proceed_day(){
        expire--;
    }


    public int getExpire() {
        return expire;
    }
}
