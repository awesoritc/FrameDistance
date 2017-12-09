public class Setting {

    //final String filename = "/Users/takuyamorimatsu/Documents/GitHub/FrameDistance/FrameDistance/Rooms/rooms1";
    //final String filename = "/Users/takuyamorimatsu/Documents/GitHub/FrameDistance/FrameDistance/Rooms/suburb_room_75_75";
    final String filename = "./Rooms/75_75_buffer";//(bufferの大きさは ceil(x_max/20)(4))

    //TODO:他のエリアに近い部屋が発生しないところまでbufferを大きくする
    //final String filename = "./Rooms/75_75_buffer_TEST";

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

    int buffer = (int)Math.ceil(x_map_size/20);
    final int[][] area_borders =
            {
                    //{x_start, x_end, y_start, y_end} (x_start<=x<x_end && y_start<=y<y_end)
                    {0, x_bottom_divider-buffer, 0, y_divider-buffer},//0
                    {x_bottom_divider+buffer, x_map_size, 0, y_divider-buffer},//1
                    {x_top_divider[1]+buffer, x_map_size, y_divider+buffer, y_map_size},//2
                    {x_top_divider[0]+buffer, x_top_divider[1]-buffer, y_divider+buffer, y_map_size},//3
                    {0, x_top_divider[0]-buffer, y_divider+buffer, y_map_size}//4
            };

    final int[] hub_point = {Math.round(x_map_size/2)/*10*/, Math.round(y_map_size/2)/*10*/};//営業所の位置


    final int goodsNum_per_room = 10;//部屋あたりの商品数
    final int[][] goods = {{0, 1, 10, 90}, {1, 1, 10, 90}, {2, 2, 10, 90}};//商品(7:2:1)
    final double[] c_value_average = {0.1, 1, 2};
    final double[] c_value_variance = {0.1, 2, 10};
    final int[] goods_distribution = {7, 2, 1};
    final int[] room_max = {30, 100, 400};//部屋ごとの最大の商品数(5:3:2) 実際はもう少し正確に
    final double[] demand_mul = {0.3, 1.0, 4.0};//需要の倍率(5:3:2)
    final int[] rooms_distribution = {5, 3, 2};
    final boolean ad_average = true;//averageに調整値を入れるかどうか((sμ, σ) にするかどうか)
    final boolean ad_c_value = false;//補正値による需要調整を入れるかどうか
    final boolean ad_max = true;//最大値を部屋倍率に合わせるかどうか

    final boolean use_poisson = true;//ポアソン分布を使うかどうか
    //TODO:最大値と需要の値をルート固定の時の売上と不足の割合で設定する(8:2, or 7:3 程度？)
    //todo:登録する商品をの需要を1種類にして行ってみる
    final double[][] lambda_poisson = {{0.03, 0.1, 0.3}, {0.1, 0.3, 1.0}, {0.3, 1.2, 4.0}};//λ [roomType][goodsType]
    final int[] max_poisson = {3, 8, 20};


    final boolean goods_fix = true;//商品の種類・登録数を固定するかどうか


    final int interval_days = 5;
    final int interval_weeks = 5;
    final int longest_interval = 25;//補充の最大間隔
    final int expire_flag_day = 20;//賞味期限のフラグを立てる日数

    final double border_rep_value = 0.07;//補充に行くかどうかを決定する補充優先度の閾値(最悪ライン)
    final int border_expected_shortage = 0;//補充に行くかどうかを決定する不足予想個数の閾値(最悪ライン)
    final int border_distance = 10000;//補充に行くかどうかを決定する距離の閾値(最悪ライン)

    //作業時間要素を追加
    final int work_time = 390;//1日の労働時間
    final double service_time_per_room = 10;//1部屋あたりの細かい移動・補充の時間
    final double move_time_per_1 = 0.5;
    final double payment_per_min = 1000/60;

    final double profit_rate = 0.3;
}
