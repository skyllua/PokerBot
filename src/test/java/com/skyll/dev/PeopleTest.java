package com.skyll.dev;

public class PeopleTest extends Thread implements Runnable {
    private String name;

    public PeopleTest(String name) {
        super(name);
        this.name = name;
    }

    @Override
    public void run() {

    }

    public void say() {
        System.out.println("My name is" + name + "!");
    }
}
