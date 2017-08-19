package game;

import javafx.geometry.Rectangle2D;

class Sprites {

    /**
     * Actually there will be just a single object of this class.
     */
    static class Player {

        int direction;
        boolean isMoving;
        boolean isFalling;
        Rectangle2D area;

        void setDirection(int dir_value) {
            direction = dir_value;
        }

        void setArea(Rectangle2D rectangle2D) {
            area = rectangle2D;
        }

        int getDirection() {
            return direction;
        }

        Rectangle2D getArea() {
            return  area;
        }
    }

    static class Arrow {

        double posX;
        double posY;
        int direction;
        Rectangle2D area;

        void setPosX(double x_value) {
            posX = x_value;
        }

        void setPosY(double y_value) {
            posY = y_value;
        }

        void setArea(Rectangle2D rectangle2D) {
            area = rectangle2D;
        }

        void setDirection(int dir_value) {
            direction = dir_value;
        }

        double getPosX() {
            return posX;
        }

        double getPosY() {
            return posY;
        }

        Rectangle2D getArea() {
            return  area;
        }

        int getDirection() {
            return direction;
        }
    }

    static class Artifact {

        double posX;
        double posY;
        Rectangle2D area;

        void setPosX(double x_value) {
            posX = x_value;
        }

        void setPosY(double y_value) {
            posY = y_value;
        }

        void setArea(Rectangle2D rectangle2D) {
            area = rectangle2D;
        }

        double getPosX() {
            return posX;
        }

        double getPosY() {
            return posY;
        }

        Rectangle2D getArea() {
            return  area;
        }
    }

    static class Ornament {

        double marginX;
        double marginY;

        void setMarginX(double x_value) {
            marginX = x_value;
        }

        void setMarginY(double y_value) {
            marginY = y_value;
        }

        double getMarginX() {
            return marginX;
        }

        double getMarginY() {
            return marginY;
        }
    }

    static class Teleport {

        double posX;
        double posY;
        Rectangle2D area;

        void setPosX(double x_value) {
            posX = x_value;
        }

        void setPosY(double y_value) {
            posY = y_value;
        }

        void setArea(Rectangle2D rectangle2D) {
            area = rectangle2D;
        }

        double getPosX() {
            return posX;
        }

        double getPosY() {
            return posY;
        }

        Rectangle2D getArea() {
            return  area;
        }

    }

    static class Key {

        double posX;
        double posY;
        Rectangle2D area;

        void setPosX(double x_value) {
            posX = x_value;
        }

        void setPosY(double y_value) {
            posY = y_value;
        }

        void setArea(Rectangle2D rectangle2D) {
            area = rectangle2D;
        }

        double getPosX() {
            return posX;
        }

        double getPosY() {
            return posY;
        }

        Rectangle2D getArea() {
            return  area;
        }

    }

    static class Door {

        double posX;
        double posY;
        Rectangle2D area;
        int orientation;

        void setPosX(double x_value) {
            posX = x_value;
        }

        void setPosY(double y_value) {
            posY = y_value;
        }

        void setArea(Rectangle2D rectangle2D) {
            area = rectangle2D;
        }

        void setOrientation(int o_value) {
            orientation = o_value;
        }

        double getPosX() {
            return posX;
        }

        double getPosY() {
            return posY;
        }

        Rectangle2D getArea() {
            return  area;
        }

        int getOrientation() {
            return orientation;
        }

    }

    static class Slot {

        double posX;
        double posY;
        Rectangle2D area;
        int orientation; // 0 = horizontal, 1 = vertical

        void setPosX(double x_value) {
            posX = x_value;
        }

        void setPosY(double y_value) {
            posY = y_value;
        }

        void setArea(Rectangle2D rectangle2D) {
            area = rectangle2D;
        }

        void setOrientation(int orientation_value) {
            orientation = orientation_value;
        }

        double getPosX() {
            return posX;
        }

        double getPosY() {
            return posY;
        }

        Rectangle2D getArea() {
            return  area;
        }

        int getOrientation() {
            return orientation;
        }
    }

    static class Life {

        double posX;
        double posY;
        Rectangle2D area;

        void setPosX(double x_value) {
            posX = x_value;
        }

        void setPosY(double y_value) {
            posY = y_value;
        }

        void setArea(Rectangle2D rectangle2D) {
            area = rectangle2D;
        }

        double getPosX() {
            return posX;
        }

        double getPosY() {
            return posY;
        }

        Rectangle2D getArea() {
            return  area;
        }
    }
}