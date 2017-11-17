public class Factory {

    //FactoryはSimulator1つに対して1つだけ

    Setting setting;

    public Factory(Setting setting) {

        this.setting = setting;
    }




    int[] currentItemId = new int[setting.goods.length]; //それぞれの商品の最新の商品番号(次の商品の番号)


    public void manufacture(int goodsNumber){

        //商品番号の商品を作成する

    }
}
