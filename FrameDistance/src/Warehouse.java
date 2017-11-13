import java.util.ArrayList;

public class Warehouse {

    private String roomId = "warehouse";

    private ArrayList<Goods> goodsList_warehouse = new ArrayList<>();

    public Warehouse(ArrayList<Goods> goodsList_warehouse) {
        this.goodsList_warehouse = goodsList_warehouse;

        //TODO:goodsListを作成し、restockする
    }



    //商品を仕入れるメソッド
    private void restock(int goods_num){
        
    }
}
