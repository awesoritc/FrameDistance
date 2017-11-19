import java.lang.reflect.Array;
import java.util.ArrayList;

public class Factory {

    //FactoryはSimulator1つに対して1つだけ

    Setting setting;
    String simulatorType;

    private int[] currentItemId;
    private ArrayList<Item>[] itemStock;

    public Factory(String simulatorType, Setting setting) {

        this.setting = setting;
        this.simulatorType = simulatorType;


        currentItemId = new int[setting.goods_num]; //それぞれの商品の最新の商品番号(次の商品の番号)
        itemStock = new ArrayList[setting.goods_num];

        for (int i = 0; i < itemStock.length; i++) {
            currentItemId[i] = 0;
            itemStock[i] = new ArrayList<Item>();
        }
    }


    //最初に置く商品を作成
    public void init(int roomId, int goodsType, int goodsNum){

        for (int i = 0; i < setting.goods[goodsType][2]; i++) {
            itemStock[goodsNum].add(new Item(currentItemId[goodsNum], goodsType, goodsNum, setting.goods[goodsType][3], roomId));
            currentItemId[goodsNum]++;
        }
    }


    //1日進める
    public void proceed_day_factory(){

        for (int i = 0; i < itemStock.length; i++) {
            for (int j = 0; j < itemStock[i].size(); j++) {
                itemStock[i].get(j).proceed_day_item();
                System.out.println("item" + i + ":" + itemStock[i].get(j).getExpire());
            }
        }
    }



    //ストックを数える
    public int count_stock_factory(int roomId, int goodsNum){

        int count = 0;
        for (int i = 0; i < itemStock[goodsNum].size(); i++) {
            if(itemStock[goodsNum].get(i).getBelongRoomId() == roomId){
                count++;
            }
        }

        return count;
    }



    //ストックの補充をする
    public void do_replenishment_factory(int roomId, int goodsType, int goodsNum, int amount){

        for (int i = 0; i < amount; i++) {
            //Item(int itemId, int goodsNum, int expire, int belongRoomId)
            Item item = new Item(currentItemId[goodsNum], goodsType, goodsNum, setting.goods[goodsType][3], roomId);
            currentItemId[goodsNum]++;
            itemStock[goodsNum].add(item);
        }
    }



    //ストックの消費をする
    public void do_consume_factory(int roomId, int goodsNum, int amount){

        int count = 0;
        //ループの途中でremoveしてはいけない？
        ArrayList<Integer> array = new ArrayList<>();
        for (int i = 0; i < itemStock[goodsNum].size(); i++) {
            if(itemStock[goodsNum].get(i).getBelongRoomId() == roomId){

                //賞味期限切れの商品の場合はエラーを返す
                if(itemStock[goodsNum].get(i).getExpire() < 0){
                    throw new RuntimeException(itemStock[goodsNum].get(i).getBelongRoomId() + "の" + goodsNum + "の商品の賞味期限切れが指摘されました");
                }

                //itemStock[goodsNum].remove(i);
                array.add(i);
                count++;

                if(count >= amount){
                    break;
                }
            }
        }

        for (int i = 0; i < array.size(); i++) {
            itemStock[goodsNum].remove(array.get(i));
        }
    }



    //賞味期限のチェック
    //賞味期限切れになった個数を返す(同時にその数分の商品を補充)
    public int check_expire(int roomId, int goodsType, int goodsNum){

        int count = 0;
        ArrayList<Integer> array = new ArrayList<>();

        for (int i = 0; i < itemStock[goodsNum].size(); i++) {
            if(itemStock[goodsNum].get(i).getExpire() < 0){
                itemStock[goodsNum].remove(i);
                array.add(i);
                itemStock[goodsNum].add(new Item(currentItemId[goodsNum], goodsType, goodsNum, setting.goods[goodsType][3], roomId));
                currentItemId[goodsNum]++;
                count++;
            }
        }

        for (int i = 0; i < array.size(); i++) {
            itemStock[goodsNum].remove(array.get(i));
        }

        return count;
    }


    //TODO:
    //個数を数える
    //ストックの補充をする
    //itemの消費をする
    //賞味期限のチェックをする



}
