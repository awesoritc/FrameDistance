public class Simulator {

    Room[] rooms;
    Setting setting;
    String simulatiorType;

    Simulator(Room[] rooms, Setting setting, String simulatiorType){

        this.rooms = rooms;
        this.setting = setting;
        this.simulatiorType = simulatiorType;

    }


    public void create_route(){}

    public void do_consume_room(){}

    public void do_replenishment(){}
}
