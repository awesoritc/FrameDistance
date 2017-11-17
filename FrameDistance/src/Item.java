public class Item {

    String id;
    int expire;
    int belongRoomId;

    public Item(String id, int expire, int belongRoomId) {
        this.id = id;
        this.expire = expire;
        this.belongRoomId = belongRoomId;
    }
}
