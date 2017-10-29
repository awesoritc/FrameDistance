public class Goods {
    Setting setting;
    String simulationType;



    int goodsType;

    Goods(int goodsType, Setting setting, String simulatorType){

        this.goodsType = goodsType;
        this.setting = setting;
        this.simulationType = simulatorType;

    }



    public void do_consume_goods(){}

    public void do_replenishment_goods(){}
}
