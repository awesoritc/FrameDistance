import java.util.ArrayList;

public class Room {
    Setting setting;
    String simulatiorType;

    int roomId, areaNumber, x_pos, y_pos;

    ArrayList<Goods> goodsList = new ArrayList<>();

    Room(int roomId, int areaNumber, int x_pos, int y_pos, Setting setting, String simulatorType){

        this.roomId = roomId;
        this.areaNumber = areaNumber;
        this.x_pos = x_pos;
        this.y_pos = y_pos;

        this.setting = setting;
        this.simulatiorType = simulatorType;

    }


    //メインで使うもの

    public void do_consume_room(){}

    public void do_replenishment_room(){}



    //使い捨てのもの
    public void register_goods(int goodsType){
        goodsList.add(new Goods(goodsType, setting, simulatiorType));
    }
}
