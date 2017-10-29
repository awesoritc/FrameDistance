public class Util {

    //次までの期間を返す
    public static int get_interval(int current_area, int area_num){

        Setting setting = new Setting();

        int interval = 0;
        if(area_num > current_area){
            interval = area_num - current_area;
        }else{
            interval = area_num + setting.area - current_area;
        }

        return interval;
    }
}
