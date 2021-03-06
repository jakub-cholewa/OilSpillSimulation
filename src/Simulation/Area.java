package Simulation;

import java.util.Random;

public class Area {

    private int size;
    private double windPower;
    private Direction windDirection;
    private double[] windDirectionsPower = new double[8];
    private double temperature;
    private Cell[][] areaGrid;
    public int sourceX = -1;
    public int sourceY = -1;
    private double overallSourceLevel = 10;


    /**
     * Area grid creation
     *
     * @param size
     */
    public Area(int size) {
        this.size = size;
        areaGrid = new Cell[this.size][this.size];
        generateRandomArea();
    }

    public void generateWindDireciontsPower() {
        int windDirection = this.windDirection.ordinal();
        this.windDirectionsPower[windDirection] = this.windPower;
        this.windDirectionsPower[(windDirection + 4) % 8] = -this.windPower;
        this.windDirectionsPower[(windDirection + 1) % 8] = this.windPower / 2;
        this.windDirectionsPower[(windDirection + 7) % 8] = this.windPower / 2;
        this.windDirectionsPower[(windDirection + 3) % 8] = -this.windPower / 2;
        this.windDirectionsPower[(windDirection + 5) % 8] = -this.windPower / 2;
        this.windDirectionsPower[(windDirection + 2) % 8] = 0;
        this.windDirectionsPower[(windDirection + 6) % 8] = 0;
    }

    public void generateRandomArea() {
        Random generator = new Random();

        for (int x = 0; x < this.size; x++)
            for (int y = 0; y < this.size; y++) {
                areaGrid[x][y] = new Cell(x, y, Type.WATER);
                if (generator.nextInt(1000) > 512)
                    areaGrid[x][y].setType(Type.LAND);
                else
                    areaGrid[x][y].setType(Type.WATER);
            }

        for (int i = 0; i < 10; i++) {
            for (int x = 0; x < this.size; x++)
                for (int y = 0; y < this.size; y++)
                    if (areaGrid[x][y].getType() == Type.LAND) {
                        if (areaGrid[x][y].getNumberOfNeighbours8Directions(this, Type.WATER) >= 4)
                            areaGrid[x][y].setType(Type.WATER);
                    } else if (areaGrid[x][y].getType() == Type.WATER)
                        if (areaGrid[x][y].getNumberOfNeighbours8Directions(this, Type.LAND) >= 4)
                            areaGrid[x][y].setType(Type.LAND);
        }

        generateCoast();
    }


    public void generateRandomSpillSource() {
        Random generator = new Random();

        while (true) {
            int x = generator.nextInt(this.size - 150);
            int y = generator.nextInt(this.size - 150);
            x += 75;
            y += 75;

            if (areaGrid[x][y].getType() == Type.WATER) {
                this.generateSpillSource(x, y);
                break;
            }
        }
    }

    /**
     * Generate spill source in given coordinates
     *
     * @param x
     * @param y
     */
    public void generateSpillSource(int x, int y) {
        areaGrid[x][y].setType(Type.SOURCE);
        this.sourceX = x;
        this.sourceY = y;
        areaGrid[x][y].setOilLevel(10.0);
    }

    public void generateRandomWCurrent() {
        Random generator = new Random();
        int x = generator.nextInt(this.size - 100);

        this.generateWCurrent(x);
    }

    /**
     * Generate current in given direction
     *
     * @param xWCurrent
     */
    public void generateWCurrent(int xWCurrent) {
        Random generator = new Random();

        double randomWCurrentPower = generator.nextDouble();
        Direction currentWDirection = Direction.values()[generator.nextInt(8)];

        for (int x = xWCurrent; x < xWCurrent + 30; x++)
            for (int y = 0; y < this.size; y++)
                areaGrid[x][y].setWCurrent(randomWCurrentPower, currentWDirection);
    }

    public void generateCoast() {
        for (int x = 0; x < this.size; x++)
            for (int y = 0; y < this.size; y++)
                areaGrid[x][y].checkCoast(this);
    }

    public void updateSource() {
        if (overallSourceLevel <= 0) return;
        overallSourceLevel -= 100.0 - getSource().getOilLevel();
        getSource().setOilLevel(100.0 - (overallSourceLevel >= 0 ? 0 : overallSourceLevel));
    }

    public void upgradeOilExpansion() {

        for (int x = 1; x < this.getSize() - 1; x++) {
            for (int y = 1; y < this.getSize() - 1; y++) {
                areaGrid[x][y].updateOilLevel();
            }
        }

        updateSource();
    }

    /**
     * Counts oilLevel in each cell
     */
    public void checkOilForCircle() {

        for (int x = 1; x < this.getSize() - 1; x++) {
            for (int y = 1; y < this.getSize() - 1; y++) {
                areaGrid[x][y].generateNewOilLevel(this);
            }
        }

        this.upgradeOilExpansion();
    }

    // need for Area setters..
    public void setSimulationParameters(String windDirection, Double windSpeed, String waterDirection, Double waterSpeed, double temperature) {
        this.windDirection = Direction.stringToDirection(windDirection);
        this.windPower = windSpeed;
        this.temperature = temperature;
        for (int x = 0; x < this.getSize(); x++) {
            for (int y = 0; y < this.getSize(); y++) {
                areaGrid[x][y].setWCurrent(waterSpeed, Direction.stringToDirection(waterDirection));
            }
        }

    }

    // need for Area getters..
    public int getSize() {
        return this.size;
    }

    private Cell getSource() {
        return areaGrid[sourceX][sourceY];
    }

    public double getTemperature() {
        return this.temperature;
    }

    public Cell getCell(int x, int y) {
        return areaGrid[x][y];
    }

    public double getWindPowerAtDirection(Direction windDirection) {
        return this.windDirectionsPower[windDirection.ordinal()];
    }

    // info display
    public void printSimulationParameters() {
        System.out.println("PARAMETRY SYMULACJI");
        System.out.println("Kierunek Wiatru = " + this.windDirection);
        System.out.println("Predkosc Wiatru = " + this.windPower);
    }
}
