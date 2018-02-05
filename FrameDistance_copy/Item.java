public class Item {

    private int expire;
    private int day_displayed;
    private int roomId;
    private int roomType;
    private int goodsType;

    public Item(int expire, int day_displayed) {
        this.expire = expire;
        this.day_displayed = day_displayed;
    }

    public void proceed_day(){
        expire--;
    }

    public int getExpire() {
        return expire;
    }

    public int getDay_displayed() {
        return day_displayed;
    }
}
