package com.khlob.onceagain

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    companion object{
        val SCREEN_HEIGHT = 1000
        val SCREEN_WIDTH = 1000
        val DELTA = 1f/10
        val MOVEMENT_SPEED = 500
        val TURN_SPEED = 45 //degrees per button press

        var canvas: Canvas = Canvas()
        var bitmap: Bitmap = Bitmap.createBitmap(SCREEN_WIDTH, SCREEN_HEIGHT, Bitmap.Config.ARGB_8888)

        var cam: mapObj = mapObj(0, 0, 100, 100)
        var angle = 0f
        var map: MutableList<mapObj> = mutableListOf()
        var ghost_locations: MutableList<Int> = mutableListOf() // the indexes of where the ghosts are located inside the map list
    }

    lateinit var start_button : Button
    lateinit var back_button : Button
    lateinit var right_button : Button
    lateinit var left_button: Button
    lateinit var turnl_button: Button
    lateinit var turnr_button: Button
    lateinit var my_img: ImageView

    lateinit var red_paint: Paint
    var paints: MutableList<Paint> = mutableListOf()
    lateinit var gray_paint: Paint
    lateinit var orange_paint: Paint
    lateinit var black_paint: Paint
    lateinit var lightGray_paint: Paint
    lateinit var cyan_paint: Paint

    var circlex = 0
    var circley = 0









    override fun onCreate(savedInstanceState: Bundle?) {
        if(true) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContentView(R.layout.activity_main)
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        for(i in -19..19){
            map.add(mapObj(-200* i, 20*200, 200, 1000))
            map.add(mapObj(-200* i, -20*200, 200, 1000))
            map.add(mapObj(20*200, 200*i, 200, 1000))
            map.add(mapObj(-20*200, 200*i, 200, 1000))
            //CREATES 157 OBJECTS OF OUTER WALL
        }
        for(i in 1..6){
            create_random_box()
            create_random_person()
        }
        for(looking in map){
            looking.check_pos() //go through all map objects and check them to make sure they aren't spawned too close to the player
        }
        /*map.add(mapObj(1000, 400, 200, 1000))
        map.add(mapObj(1000, 600, 200, 1000))
        map.add(mapObj(250, 500, 200, 1000))
        map.add(mapObj(250, 700, 200, 1000))
        map.add(mapObj(250, 900, 200, 1000))
        map.add(mapObj(250, 1100, 200, 1000))*/
        setPaints()
        setupCanvas()

        start_button = findViewById(R.id.button_starter)
        back_button = findViewById(R.id.button_back)
        right_button = findViewById(R.id.button_right)
        left_button = findViewById(R.id.button_left)
        turnl_button = findViewById(R.id.button_turnl)
        turnr_button = findViewById(R.id.button_turnr)
        start_button.setOnClickListener {
            /*cam.z+= (100 * Math.cos(angle.toDouble())).toInt()
            cam.x+= (100 * Math.sin(angle.toDouble())).toInt()*/
            move_fwd((1/ DELTA).toInt())
            render_full(map)
        }
        back_button.setOnClickListener {
            move_bckwd((1/ DELTA).toInt())
            render_full(map)
        }

        right_button.setOnClickListener {
            move_rt((1/ DELTA).toInt())
            render_full(map)
        }
        left_button.setOnClickListener {
            move_lt((1/ DELTA).toInt())
            render_full(map)
        }
        turnl_button.setOnClickListener {
            rot_lt((1/ DELTA).toInt())
            render_full(map)
        }
        turnr_button.setOnClickListener {
            rot_rt((1/ DELTA).toInt())
            render_full(map)
        }

        //the_stuff()
        render_full(map)
        ghost_movements()
    }
    fun create_random_box(){
        val centerx = Random.nextInt(-20, 20)*200
        val centery = Random.nextInt(-20, 20)*200
        val size = Random.nextInt(2, 6)
        val wall_to_ignore = Random.nextInt(1, 4)
        for(i in -size..size){
            if(wall_to_ignore!=1)map.add(mapObj(centerx + 200 * -i, centery + 200 * size, 200, 1000))
            if(wall_to_ignore!=2)map.add(mapObj(centerx + 200 * -i, centery - 200 * size, 200, 1000))
            if(wall_to_ignore!=3)map.add(mapObj(centerx + 200 * size, centery + 200 * i, 200, 1000))
            if(wall_to_ignore!=4)map.add(mapObj(centerx - 200 * size, centery + 200 * i, 200, 1000))
        }
    }
    fun create_random_person(){
        val centerx = Random.nextInt(-20, 20)*200
        val centery = Random.nextInt(-20, 20)*200
        map.add(mapObj(centerx, centery, 200, 1000, true))
        ghost_locations.add(map.size-1)
    }

    fun setPaints(){
        red_paint =
            Paint().apply {
                isAntiAlias = false
                color = Color.RED
                style = Paint.Style.FILL
            }
        gray_paint =
            Paint().apply {
                isAntiAlias = false
                color = Color.DKGRAY
                style = Paint.Style.FILL
            }
        orange_paint =
            Paint().apply {
                isAntiAlias = false
                color = Color.rgb(255,95,31)
                style = Paint.Style.FILL
            }
        black_paint =
            Paint().apply {
                isAntiAlias = false
                color = Color.BLACK
                style = Paint.Style.FILL
            }
        lightGray_paint =
            Paint().apply {
                isAntiAlias = false
                color = Color.LTGRAY
                style = Paint.Style.FILL
            }
        cyan_paint =
            Paint().apply {
                isAntiAlias = false
                color = Color.CYAN
                style = Paint.Style.FILL
            }
        var i = 0
        while(i < 5){
            paints.add(Paint().apply {
                isAntiAlias = false
                color = Color.rgb(255-i*50, 0, 0)
                style = Paint.Style.FILL
            })
            paints.add(Paint().apply {
                isAntiAlias = false
                color = Color.rgb(0, 255-i*50, 0)
                style = Paint.Style.FILL
            })
            paints.add(Paint().apply {
                isAntiAlias = false
                color = Color.rgb(0, 0, 255-i*50)
                style = Paint.Style.FILL
            })
            i++
        }

        var j = 0
        while(j < map.size){
            map[j].paint = paints[j%paints.size]
            j++
        }
    }
    fun clear_canvas(){
        canvas.drawARGB(255, 0, 0, 0)
        //refresh_canvas()
    }
    fun setupCanvas(){
        my_img = findViewById(R.id.imageView_img)
        canvas = Canvas(bitmap)
        clear_canvas()
    }

    fun the_stuff(){
        canvas.drawCircle(10f,10f, 100f, red_paint)
        refresh_canvas()
    }

    fun refresh_canvas(){
        my_img.setImageBitmap(bitmap)
    }


    fun drawCustomRect(x: Int, y: Int, width: Int, height: Int){
        canvas.drawRect((SCREEN_WIDTH/2+x-width/2).toFloat(), (SCREEN_HEIGHT/2-y-height/2).toFloat(), (SCREEN_WIDTH/2+x+width/2).toFloat(), (SCREEN_HEIGHT/2-y+height/2).toFloat(), red_paint)
        //Log.d("RENDER", "Drew rect @\nx="+x+"\ny="+y+"\nwidth="+width+"\nheight="+height)
    }
    fun drawCustomRect(x: Float, y: Float, width: Float, height: Float, paint: Paint){
        var faded_paint = paint
        canvas.drawRect((SCREEN_WIDTH/2+x-width/2).toFloat(), (SCREEN_HEIGHT/2-y-height/2).toFloat(), (SCREEN_WIDTH/2+x+width/2).toFloat(), (SCREEN_HEIGHT/2-y+height/2).toFloat(), paint)
        //Log.d("RENDER", "Drew rect @\nx="+x+"\ny="+y+"\nwidth="+width+"\nheight="+height)
    }

    fun render_full(given_map: MutableList<mapObj>){

        //val rot_map = create_rot_map(given_map)
        val local_map = create_local_map(given_map)
        //val local_map = create_local_map(rot_map)
        val rot_map = create_rot_map(local_map)

        render(rot_map)
        //render(local_map)
    }
    fun create_local_map(given_map: MutableList<mapObj>): MutableList<mapObj>{
        var local_map: MutableList<mapObj> = mutableListOf()
        for(adding in given_map){
            var moved = mapObj()

            moved.width = adding.width
            moved.height = adding.height
            moved.paint = adding.paint
            moved.isPerson = adding.isPerson

            moved.x = adding.x - cam.x
            moved.z = adding.z - cam.z

            local_map.add(moved)
        }
        return local_map
    }
    fun create_rot_map(given_map: MutableList<mapObj>): MutableList<mapObj>{
        //render(map)
        var rot_map: MutableList<mapObj> = mutableListOf()
        for(adding in given_map){
            var rotated = mapObj()
            rotated.width = adding.width
            rotated.height = adding.height
            rotated.paint = adding.paint
            rotated.isPerson = adding.isPerson

            rotated.x = ((Math.sin(angle.toDouble())*adding.z - Math.cos(angle.toDouble())*adding.x)).toInt()
            rotated.z = ((Math.sin(angle.toDouble())*adding.x  +  Math.cos(angle.toDouble())*adding.z)).toInt()

            rot_map.add(rotated)
        }
        return rot_map
    }
    fun render(given_map: MutableList<mapObj>){
        //using local and rot maps
        clear_canvas()
        drawCustomRect(0f, -250f, 1000f, 500f, gray_paint)
        given_map.sortByDescending { it.z}
        for(rendering in given_map){
            if(rendering.z>0){
                if(!rendering.isPerson)
                {
                    drawCustomRect((rendering.x)/(rendering.z*2f) * SCREEN_WIDTH, 0f, rendering.width / ((rendering.z)*2f) * SCREEN_WIDTH,1f*rendering.height / (rendering.z) * SCREEN_HEIGHT, rendering.paint )
                } else
                {
                    drawCustomRect((rendering.x)/(rendering.z*2f) * SCREEN_WIDTH, 0f, 400f / ((rendering.z)*2) * SCREEN_WIDTH,400f / (rendering.z) * SCREEN_HEIGHT, orange_paint)
                    drawCustomRect((rendering.x)/(rendering.z*2f) * SCREEN_WIDTH, (-350)/(rendering.z*2f) * SCREEN_WIDTH, 200f / ((rendering.z)*2) * SCREEN_WIDTH,300f / (rendering.z) * SCREEN_HEIGHT, orange_paint)
                    //Log.d("g", "drew orng")
                }
            }
        }
        draw_sword(250, -250)
        refresh_canvas()
    }

    fun draw_sword(x: Int, y: Int){
        val centerx = x.toFloat()
        val centery = y.toFloat()
        drawCustomRect(centerx, centery, 300f, 50f, cyan_paint)
        drawCustomRect(centerx, centery, 200f, 50f, black_paint)
        drawCustomRect(centerx, centery-100, 100f, 150f, black_paint)
    }

    //old rendering method. Did not work with rotation. Automatically gave local position values given a world space map
    fun old_render(given_map: MutableList<mapObj>){
        clear_canvas()
        given_map.sortByDescending { it.z - cam.z }
        for(rendering in given_map){
            if(rendering.z>cam.z)drawCustomRect((rendering.x-cam.x)/((rendering.z-cam.z)*2f) * SCREEN_WIDTH, 0f, rendering.width / ((rendering.z-cam.z)*2f) * SCREEN_WIDTH,1f*rendering.height / (rendering.z-cam.z) * SCREEN_HEIGHT, rendering.paint )
        }
        refresh_canvas()
    }

    fun move_fwd(i: Int){
        var new_i = i
        if(new_i<=0)return
        Handler(Looper.getMainLooper()).postDelayed({

            val plusz = (MOVEMENT_SPEED * DELTA * Math.cos(angle.toDouble())).toInt()
            val plusx = (MOVEMENT_SPEED * DELTA * Math.sin(angle.toDouble())).toInt()

            //move_check moves the camera and checks for collision
            if(!move_check(plusx, plusz, map)) new_i = 0 // if collided into something (move check returns false), end the movement
            render_full(map)
            move_fwd(new_i-1)
        }, DELTA.toLong()*1000)
    }
    fun move_bckwd(i: Int){
        var new_i = i
        if(new_i<=0)return
        Handler(Looper.getMainLooper()).postDelayed({
            val plusz = -(MOVEMENT_SPEED * DELTA * Math.cos(angle.toDouble())).toInt()
            val plusx= -(MOVEMENT_SPEED * DELTA * Math.sin(angle.toDouble())).toInt()
            if(!move_check(plusx, plusz, map)) new_i = 0
            render_full(map)
            move_bckwd(new_i-1)
        }, DELTA.toLong()*1000)
    }
    fun move_rt(i: Int){
        var new_i = i
        if(new_i<=0)return
        Handler(Looper.getMainLooper()).postDelayed({
            val plusx = -(MOVEMENT_SPEED * DELTA * Math.cos(angle.toDouble())).toInt()
            val plusz = (MOVEMENT_SPEED * DELTA * Math.sin(angle.toDouble())).toInt()
            if(!move_check(plusx, plusz, map)) new_i = 0
            render_full(map)
            move_rt(new_i-1)
        }, DELTA.toLong()*1000)
    }
    fun move_lt(i: Int){
        var new_i = i
        if(new_i<=0)return
        Handler(Looper.getMainLooper()).postDelayed({
            val plusx = (MOVEMENT_SPEED * DELTA * Math.cos(angle.toDouble())).toInt()
            val plusz = -(MOVEMENT_SPEED * DELTA * Math.sin(angle.toDouble())).toInt()
            if(!move_check(plusx, plusz, map)) new_i = 0
            render_full(map)
            move_lt(new_i-1)
        }, DELTA.toLong()*1000)
    }
    fun rot_rt(i: Int){
        if(i<=0)return
        Handler(Looper.getMainLooper()).postDelayed({
            angle-=(TURN_SPEED*Math.PI/180f * DELTA).toFloat()
            render_full(map)
            rot_rt(i-1)
        }, DELTA.toLong()*1000)
    }
    fun rot_lt(i: Int){
        if(i<=0)return
        Handler(Looper.getMainLooper()).postDelayed({
            angle+=(TURN_SPEED*Math.PI/180f * DELTA).toFloat()
            render_full(map)
            rot_lt(i-1)
        }, DELTA.toLong()*1000)
    }


    fun move_check(plusx: Int, plusz: Int, given_map: MutableList<mapObj>): Boolean{
        //consider making it so that it only checks from the 158th map object onwards (because the outer walls take up the first 157 objects), and do a simple x >= 20*200 check for outer walls
        cam.x += plusx
        cam.z += plusz

        //"round" to nearest 200 using int division. 123 -> 0, and 201 -> 200, and 1255 -> 1200
        val approxX = cam.x / 200 * 200
        val approxZ = cam.z / 200 * 200
        //Log.d("T", "approx x: "+approxX + ", approx z: "+approxZ)

        for(checking in given_map){
            //collision detected
            if(!checking.isPerson && approxX == checking.x && approxZ == checking.z){
                cam.x -= plusx
                cam.z -= plusz
                return false
            }
        }
        return true
    }







    fun ghost_movements(){
        Handler(Looper.getMainLooper()).postDelayed({

            for(i in ghost_locations){
                var looking_ghost = map[i]
                val plusx = Random.nextInt(-1, 1) * MOVEMENT_SPEED * 1
                val plusy = Random.nextInt(-1, 1) * MOVEMENT_SPEED * 1

                looking_ghost.x += plusx
                looking_ghost.z += plusy

                if(Math.abs(looking_ghost.x) > 20 * 200 || Math.abs(looking_ghost.z)> 20 * 200){
                    looking_ghost.x = Random.nextInt(-20, 20) * 200
                    looking_ghost.z = Random.nextInt(-20, 20) * 200
                }
            }

            render_full(map)
            ghost_movements()
        }, 1000)
    }
}
