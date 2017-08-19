package game;

import javafx.geometry.Rectangle2D;

class Sprites {

    static class Player {

        int direction;
        boolean isMoving;
        boolean isFalling;
        Rectangle2D area;

        void setDirection(int dir_value) {
            direction = dir_value;
        }

        int getDirection() {
            return direction;
        }

        void setArea(Rectangle2D rectangle2D) {
            area = rectangle2D;
        }

        Rectangle2D getArea() {
            return  area;
        }
        
    }
}