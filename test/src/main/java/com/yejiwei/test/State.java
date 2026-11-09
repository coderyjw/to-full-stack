package com.yejiwei.test;

public enum State {
    //WEIFUKUAN,//State WEIFUKUAN = new State();
    //YIFUKUAN,//State YIFUKUAN = new State();
    //WEIFAHUO,//State WEIFAHUO = new State();
    //YIFAHUO;//State YIFAHUO = new State();

    WEIFUKUAN("未付款"),//State WEIFUKUAN = new State("未付款");
    YIFUKUAN("已付款"),//State YIFUKUAN = new State("已付款");
    WEIFAHUO("未发货"),//State WEIFAHUO = new State("未发货");
    YIFAHUO("已发货");//State YIFAHUO = new State("已发货");
    String name;
    State(){
    }
    State(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}