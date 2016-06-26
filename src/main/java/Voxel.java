/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author xcha011
 */
public class Voxel{

    public int x;
    public int y;
    public int z;

    public Voxel(){
        x = y = z = 0;
    }

    public Voxel(int z, int y, int x){
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
