public class Setting {

    final String filename = "rooms1";

    final String simulatorType_dynamic = "dynamic";
    final String simulatorType_static = "static";

    final int day = 100;//日数
    final int area = 5;//エリア数


    final int[][] goods = {{0, 1, 10}, {1, 1, 10}, {2, 2, 10}};//商品(7:2:1)
    final int[] max_room = {30, 100, 400};//部屋ごとの最大の商品数(5:3:2) 実際はもう少し正確に
    final double[] demand_mul = {0.3, 1.0, 4.0};//需要の倍率(5:3:2)




    final int interval_days = 5;
    final int interval_weeks = 5;
}
