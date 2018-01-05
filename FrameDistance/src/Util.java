import java.io.*;

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




    public static int[][] read_room_file(String filename, Setting setting){

        //room_file読み込み
        Room[] rooms = new Room[setting.room];

        int[][] ret = new int[setting.room/*100*/][5/**/];//100部屋の5つの要素を返す


        //forここから
        try{
            BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));

            for (int i = 0; i < setting.room; i++) {
                String tmp_room = reader.readLine();
                String[] a = tmp_room.split(",");

                int roomid = Integer.valueOf(a[0]);
                int areanumber = Integer.valueOf(a[1]);
                String[] pos = a[2].split(":");
                int x_pos = Integer.valueOf(pos[0].substring(1));
                int y_pos = Integer.valueOf(pos[1].substring(0, pos[1].length()-1));
                int itemnumber = Integer.valueOf(a[3]);

                ret[i] = new int[]{roomid, areanumber, x_pos, y_pos, itemnumber};
            }

            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        //Roomの要素を返却
        return ret;
    }



    public static int[][] read_gravity_file(String filename, Setting setting){

        int[][] ret = new int[setting.area][2];

        try{
            BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
            for (int i = 0; i < setting.area; i++) {
                String[] tmp = reader.readLine().split(",");
                ret[i][0] = Integer.valueOf(tmp[0]);
                ret[i][1] = Integer.valueOf(tmp[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public static void write_gravity(int roomId, int areaNum, int[] distances){

        try{
            //new FileWriter(new File("distances_to_gravity.csv")).write("roomId,areaNum,toArea,distance");
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(new File("distances_to_gravity.csv"), true)));
            for (int i = 0; i < distances.length; i++) {
                pw.write(roomId + "," + areaNum + "," + i + "," + distances[i] + "\n");
            }
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
