package com.khlob.onceagain

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.transition.Visibility
import java.util.Vector
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
        var mode = 0
        var angle = 0f
        var map: MutableList<mapObj> = mutableListOf()
        var ghost_locations: MutableList<Int> = mutableListOf() // the indexes of where the ghosts are located inside the map list
        var score = 0
        var time = 60
        var gameOver = false
    }
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var explodePlayer: MediaPlayer
    lateinit var start_button : Button
    lateinit var back_button : Button
    lateinit var right_button : Button
    lateinit var left_button: Button
    lateinit var turnl_button: Button
    lateinit var turnr_button: Button
    lateinit var atk_button: Button
    lateinit var my_img: ImageView
    lateinit var scoreboard: TextView
    lateinit var timer: TextView
    lateinit var timeup: TextView
    lateinit var home_buttom: ImageButton

    lateinit var red_paint: Paint
    var paints: MutableList<Paint> = mutableListOf()
    lateinit var gray_paint: Paint
    lateinit var orange_paint: Paint
    lateinit var black_paint: Paint
    lateinit var lightGray_paint: Paint
    lateinit var cyan_paint: Paint

    var attacking = false
    var sword_normal_posx = 250
    var sword_normal_posy = -250
    var sword_attack_posx = 125
    var sword_attack_posy = -125
    var hitting = false



    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if(gameOver)return true
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.unicodeChar.toChar()) {
                'w' -> {
                    start_button.performClick()
                    return true
                }
                's' -> {
                    back_button.performClick()
                    return true
                }
                'a' -> {
                    left_button.performClick()
                    return true
                }
                'd' -> {
                    right_button.performClick()
                    return true
                }
                'q' -> {
                    turnl_button.performClick()
                    return true
                }
                'i' -> {
                    turnl_button.performClick()
                    return true
                }
                'e' -> {
                    turnr_button.performClick()
                    return true
                }
                'p' -> {
                    turnr_button.performClick()
                    return true
                }
                'f' -> {
                    atk_button.performClick()
                    return true
                }
                'o' -> {
                    atk_button.performClick()
                    return true
                }
            }
        }
        return false
    }




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
        gameOver = false
        if (!this::mediaPlayer.isInitialized) {
            mediaPlayer = MediaPlayer.create(this, R.raw.music2)
            mediaPlayer.start()
        }
        if (!this::explodePlayer.isInitialized) {
            explodePlayer = MediaPlayer.create(this, R.raw.explode)
        }

        cam = mapObj(0, 0, 100, 100)
        map = mutableListOf()
        ghost_locations = mutableListOf()
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
        atk_button = findViewById(R.id.button_atk)
        scoreboard = findViewById(R.id.textView_score)
        timer = findViewById(R.id.textView_timer)
        timeup = findViewById(R.id.textView_timesup)
        home_buttom = findViewById(R.id.imageButton_home)
        do_time()
        scoreboard.text = "Score: 0"
        if(mode==0){
            time=60
        }else if(mode==1){
            time=0
        }
        timer.text = "Time: 60"
        timeup.visibility = View.INVISIBLE
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
        home_buttom.setOnClickListener {
            finish()
            mediaPlayer.stop()
            val intent = Intent(this, TitleActivity::class.java)
            startActivity(intent)
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

        atk_button.setOnClickListener {
            start_attack()
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
        if(attacking){
            if(hitting){
                drawCustomRect(sword_attack_posx.toFloat(), sword_attack_posy.toFloat()+200, 200f, 600f, red_paint)
            }
            draw_sword(sword_attack_posx, sword_attack_posy)
        } else{
            draw_sword(sword_normal_posx, sword_normal_posy)
        }

        refresh_canvas()
    }

    fun draw_sword(x: Int, y: Int){
        val centerx = x.toFloat()
        val centery = y.toFloat()
        drawCustomRect(centerx, centery, 300f, 50f, cyan_paint)
        drawCustomRect(centerx, centery, 200f, 50f, black_paint)
        drawCustomRect(centerx, centery-100, 100f, 150f, black_paint)
        drawCustomRect(centerx, centery+ 125, 100f, 200f, lightGray_paint)
        drawCustomRect(centerx, centery+ 225+100, 75f, 200f, lightGray_paint)
        drawCustomRect(centerx, centery+ 25+ 150, 15f, 300f, gray_paint)
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
        if(gameOver)return

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
            if(!gameOver)ghost_movements()
        }, 1000)
    }







    fun start_attack(){
        attacking = true
        hit_reg()
        render_full(map)

        Handler(Looper.getMainLooper()).postDelayed({
            attacking = false
            hitting = false
            render_full(map)
        }, 500)
    }
    fun hit_reg(){
        hitting = false
        for(i in ghost_locations){
            //map[i] is the ghost the code is currently looking at
            if(Math.abs(cam.x - map[i].x) <=1000 && Math.abs(cam.z-map[i].z)<=1000){
                map[i].x = Random.nextInt(-20, 20) * 200
                map[i].z = Random.nextInt(-20, 20) * 200
                score+=1
                scoreboard.text = "Score: "+ score
                hitting = true
                explodePlayer.start()
            }
        }
    }

    fun do_time(){
        if(gameOver)return

        if(mode==0){
            time -= 1
        } else if(mode==1){
            time++
        }

        timer.text = "Time: "
        if(time<10) timer.text = timer.text.toString() + "0"
        timer.text = timer.text.toString() + time

        if(mode==0 && time<=0) {
            gameOver = true
            timeup.visibility = View.VISIBLE
            end_game()
            return
        }

        if(!gameOver){
            Handler(Looper.getMainLooper()).postDelayed({
                do_time()
            }, 1000)
        }
    }

    fun end_game(){
        atk_button.visibility = View.GONE
        start_button.visibility = View.GONE
        turnr_button.visibility = View.GONE
        turnl_button.visibility = View.GONE
        back_button.visibility = View.GONE
        left_button.visibility = View.GONE
        right_button.visibility = View.GONE

        //put the thing that shows the "time up" screen and the "main menu" button
    }
}
