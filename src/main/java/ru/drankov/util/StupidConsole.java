package ru.drankov.util;


public class StupidConsole  implements Consolable{
    @Override
    public void cout(String st) {
        System.out.println(st);
    }
}
