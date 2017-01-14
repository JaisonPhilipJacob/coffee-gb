package eu.rekawek.coffeegb.gpu.phase;

import eu.rekawek.coffeegb.AddressSpace;
import eu.rekawek.coffeegb.gpu.GpuRegister;
import eu.rekawek.coffeegb.gpu.Lcdc;
import eu.rekawek.coffeegb.memory.MemoryRegisters;

public class OamSearch implements GpuPhase {

    private enum State {
        READING_Y, READING_X;
    }

    public static class SpritePosition {

        private final int x;

        private final int y;

        private final int address;

        public SpritePosition(int x, int y, int address) {
            this.x = x;
            this.y = y;
            this.address = address;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getAddress() {
            return address;
        }
    }

    private final AddressSpace oemRam;

    private final MemoryRegisters registers;

    private final SpritePosition[] sprites;

    private final Lcdc lcdc;

    private int spritePosIndex;

    private State state;

    private int spriteY;

    private int spriteX;

    private int i;

    public OamSearch(AddressSpace oemRam, MemoryRegisters registers) {
        this.oemRam = oemRam;
        this.registers = registers;
        this.lcdc = new Lcdc(registers);

        this.sprites = new SpritePosition[10];
        this.state = State.READING_Y;
    }

    @Override
    public boolean tick() {
        int spriteAddress = 0xfe00 + 4 * i;

        switch (state) {
            case READING_Y:
                spriteY = oemRam.getByte(spriteAddress);
                state = State.READING_X;
                break;

            case READING_X:
                spriteX = oemRam.getByte(spriteAddress + 1);
                if (between(spriteY, registers.get(GpuRegister.LY) + 16, spriteY + lcdc.getSpriteHeight())) {
                    sprites[spritePosIndex++] = new SpritePosition(spriteX, spriteY, spriteAddress);
                }
                i++;
                state = State.READING_Y;
                break;
        }
        return i < 40;
    }

    public SpritePosition[] getSprites() {
        return sprites;
    }

    private static boolean between(int from, int x, int to) {
        return from <= x && x < to;
    }

}