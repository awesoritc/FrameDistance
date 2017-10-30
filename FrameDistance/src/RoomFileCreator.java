import java.io.*;
import java.util.Random;

public class RoomFileCreator {

    public static void main(String[] args){

        //部屋の設定ファイルを作成する




        //id, areanumber, x_pos, y_pos, roomtype,gravity_points[][]



        Setting setting = new Setting();


        String filename = setting.filename;


        //部屋番号、エリア番号、座標、登録する商品番号

        int a = 0;
        try{

            PrintWriter pw_rooms = new PrintWriter(new BufferedWriter(new FileWriter(new File(filename+".csv"), true)));
            PrintWriter pw_gravity = new PrintWriter(new BufferedWriter(new FileWriter(new File(filename+"_gravity.csv"), true)));

            for(int i = 0; i < setting.area; i++){

                int total_x_pos = 0;
                int total_y_pos = 0;

                for(int j = 0; j < ((setting.room/setting.area)); j++){

                    Random random = new Random();
                    int x_start = setting.area_borders[i][0];
                    int x_end = setting.area_borders[i][1];
                    int y_start = setting.area_borders[i][2];
                    int y_end = setting.area_borders[i][3];

                    int x_pos = random.nextInt(x_end - x_start) + x_start;
                    int y_pos = random.nextInt(y_end - y_start) + y_start;

                    int[] pos = new int[]{x_pos, y_pos};

                    Random rand = new Random();
                    int ranInt = rand.nextInt(10);
                    int roomType;
                    if(ranInt < setting.rooms_distribution[0]){
                        roomType = 0;
                    }else if(ranInt < setting.rooms_distribution[0] + setting.rooms_distribution[1]){
                        roomType = 1;
                    }else{
                        roomType = 2;
                    }

                    // id,area_number,(x_pos:y_pos),roomType
                    pw_rooms.write(String.valueOf(a) + "," + String.valueOf(i) + "," +
                            "(" + String.valueOf(pos[0]) + ":" + String.valueOf(pos[1]) + ")," + String.valueOf(roomType) + "\n");

                    total_x_pos += pos[0];
                    total_y_pos += pos[1];

                    a++;
                }

                //gravityファイル書き出し
                int gravity_x = Math.round(total_x_pos /(int)(setting.room/setting.area));
                int gravity_y = Math.round(total_y_pos /(int)(setting.room/setting.area));
                pw_gravity.write(gravity_x + "," + gravity_y + "\n");
            }

            pw_rooms.close();
            pw_gravity.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
