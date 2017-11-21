public class Setting {

    //final String filename = "rooms1";
    //final String filename = "rooms2";
    //final String filename = "large_room";
    final String filename = "suburb_room_75_75";

    final String simulatorType_dynamic = "dynamic";
    final String simulatorType_static = "static";

    final String routeType_value = "value";
    final String routeType_greedy = "greedy";

    final String routeType = routeType_value;

    final int day = 100;//日数
    final int room = 100;//部屋の数
    final int area = 5;//エリア数
    final int rooms_area = Math.round(room/area);//エリアあたりの部屋数
    final int limit = 20;//1日に回れる部屋の数

    final int x_map_size = 75;//20;//mapの大きさx
    final int y_map_size = 75;//20;//mapの大きさy


    //正方形での分割
    int x_bottom_divider = Math.round(x_map_size/2);
    int[] x_top_divider = {Math.round(x_map_size/3)/*6*/, Math.round((x_map_size/3)*2)/*13*/};
    int y_divider = Math.round((y_map_size/5)*2)/*8*/;

    final int[][] area_borders =
            {
                    //{x_start, x_end, y_start, y_end} (x_start<=x<x_end && y_start<=y<y_end)
                    {0, x_bottom_divider, 0, y_divider},
                    {x_bottom_divider, x_map_size, 0, y_divider},
                    {x_top_divider[1], x_map_size, y_divider, y_map_size},
                    {x_top_divider[0], x_top_divider[1], y_divider, y_map_size},
                    {0, x_top_divider[0], y_divider, y_map_size}
            };

    final int[] hub_point = {Math.round(x_map_size/2)/*10*/, Math.round(y_map_size/2)/*10*/};//営業所の位置


    final int goodsNum_per_room = 10;//部屋あたりの商品数
    final int[][] goods = {{0, 1, 10, 90}, {1, 1, 10, 90}, {2, 2, 10, 90}};//商品(7:2:1)
    final int[] goods_distribution = {7, 2, 1};
    final int[] room_max = {30, 100, 400};//部屋ごとの最大の商品数(5:3:2) 実際はもう少し正確に
    final double[] demand_mul = {0.3, 1.0, 4.0};//需要の倍率(5:3:2)
    final boolean ad_average = true;//averageに調整値を入れるかどうか((sμ, σ) にするかどうか)
    final boolean ad_max = true;
    //TODO:倍率ではなく、需要の発生確率の平均に、売れやすい部屋では数字を足す
    final int[] c_value = {-1, 0, 2};
    final int[] rooms_distribution = {5, 3, 2};




    final int interval_days = 5;
    final int interval_weeks = 5;
    final int longest_interval = 25;//補充の最大間隔
    //final int limit_expire = 40;//取り替え日数(25日を切ったら廃棄処分)
    final int expire_flag_day = 20;//賞味期限のフラグを立てる日数

    //作業時間要素を追加
    final int work_time = 390;//1日の労働時間
    final double service_time_per_room = 10;//1部屋あたりの細かい移動・補充の時間
    final double move_time_per_1 = 0.5;
}
