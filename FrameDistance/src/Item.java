public class Item {

    private int goodsType;
    private int goodsNum;
    private int itemId;
    private int expire;
    private int belongRoomId;

    public Item(int itemId, int goodsType, int goodsNum, int expire, int belongRoomId) {
        this.itemId = itemId;
        this.goodsType = goodsType;
        this.goodsNum = goodsNum;
        this.expire = expire;
        this.belongRoomId = belongRoomId;
    }


    public void proceed_day_item(){
        this.expire--;
    }




    public int getItemId() {
        return itemId;
    }

    public int getExpire() {
        return expire;
    }

    public int getBelongRoomId() {
        return belongRoomId;
    }
}
