public class Setting {

    final String filename = "rooms1";

    final String simulatorType_dynamic = "dynamic";
    final String simulatorType_static = "static";

    final int day = 100;//日数
    final int room = 100;//部屋の数
    final int area = 5;//エリア数
    final int rooms_area = 20;//エリアあたりの部屋数
    final int limit = 20;//1日に回れる部屋の数

    final int x_map_size = 20;//mapの大きさx
    final int y_map_size = 20;//mapの大きさy

    int x_bottom_divider = 10;
    int[] x_top_divider = {6, 13};
    int y_divider = 8;

    final int[][] area_borders =
            {
                    //{x_start, x_end, y_start, y_end} (x_start<=x<x_end && y_start<=y<y_end)
                    {0, x_bottom_divider, 0, y_divider},
                    {x_bottom_divider, x_map_size, 0, y_divider},
                    {x_top_divider[1], x_map_size, y_divider, y_map_size},
                    {x_top_divider[0], x_top_divider[1], y_divider, y_map_size},
                    {0, x_top_divider[0], y_divider, y_map_size}
            };


    final int[][] goods = {{0, 1, 10}, {1, 1, 10}, {2, 2, 10}};//商品(7:2:1)
    final int[] goods_distribution = {7, 2, 1};
    final int[] room_max = {30, 100, 400};//部屋ごとの最大の商品数(5:3:2) 実際はもう少し正確に
    final double[] demand_mul = {0.3, 1.0, 4.0};//需要の倍率(5:3:2)
    final int[] rooms_distribution = {5, 3, 2};




    final int interval_days = 5;
    final int interval_weeks = 5;
}
