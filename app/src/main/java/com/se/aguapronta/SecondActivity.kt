package com.se.aguapronta

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.actions.ReserveIntents
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.se.aguapronta.databinding.SecondActivityBinding
import com.se.aguapronta.fragments.StateFragment
import com.se.aguapronta.fragments.WaterFragment
import com.se.aguapronta.fragments.adapters.ViewPagerAdapter
import java.lang.Math.ceil



class SecondActivity : AppCompatActivity() {
    private lateinit var binding : SecondActivityBinding
    private lateinit var firebaseAuth: FirebaseAuth
    public var brew = false
    public var aval = false
    public var curWater = 0
    public var countreserv = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.second_activity)
        binding = SecondActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        var currentUserId = FirebaseAuth.getInstance().uid


        val email = firebaseAuth.currentUser?.email
        Toast.makeText(this@SecondActivity, "Logged in as $email", Toast.LENGTH_LONG).show()

        setUpTabs()

        var databaseWater = FirebaseDatabase.getInstance().getReference("kettle")

        var databaseReservSt = FirebaseDatabase.getInstance().getReference("reservations")
        var databaseUsrReserv = FirebaseDatabase.getInstance().getReference("user-reservations")
        var databaseNrRes = FirebaseDatabase.getInstance().getReference("user-reservations").child("$currentUserId")

        var getdata = object : ValueEventListener {

            override fun onCancelled(databaseError: DatabaseError) {

            }

            @SuppressLint("UseCompatLoadingForDrawables")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var sb = StringBuilder()

                /** WATER UPDATE **/
                var cur_water = dataSnapshot.child("cur_water").getValue()
                sb.append("$cur_water")
                val watercl = findViewById(R.id.watercl) as TextView
                watercl.text = sb.toString()
                curWater = sb.toString().toInt()
                /** WATER UPDATE **/


                /** Availability **/
                //var cur_water2 = dataSnapshot.child("cur_water").getValue().toString().toInt()
                var status =  dataSnapshot.child("status").getValue()
                var brewing =  dataSnapshot.child("brewing").getValue().toString()

                sb.delete(0,sb.length)
                sb.append("$status")
                val statusapp = findViewById(R.id.state) as TextView
                statusapp.text = sb.toString()


                if(brewing.equals("Brewing", ignoreCase = true)){
                    aval = false
                    brew = true
                    val avalsymb2 = findViewById(R.id.avalsymb2) as ImageView
                    avalsymb2.setImageResource(R.drawable.cross)
                }

                else if(brewing.equals("Not Brewing", ignoreCase = true)){
                    aval=true
                    brew = false
                    val avalsymb2 = findViewById(R.id.avalsymb2) as ImageView
                    avalsymb2.setImageResource(R.drawable.nocross)
                }


                if(curWater < 27 || brewing.equals("Brewing", ignoreCase = true)){
                    aval = false
                    val avalsymb1 = findViewById(R.id.avalsymb1) as ImageView
                    avalsymb1.setImageResource(R.drawable.nocross)
                }

                else if(curWater > 27 && status.toString().equals("Idle", ignoreCase = true)  && brewing.equals("Not Brewing", ignoreCase = true)){
                    aval = true
                    val avalsymb1 = findViewById(R.id.avalsymb1) as ImageView
                    avalsymb1.setImageResource(R.drawable.cross)
                }
                /** Availability **/
                //ceil()

                /** Number of possible Cups **/
                var cur_water3 = dataSnapshot.child("cur_water").getValue().toString().toInt()
                var numbertea = ceil((cur_water3/27).toDouble())
                val possiblecup = findViewById(R.id.possiblecup) as TextView
                possiblecup.text = numbertea.toString()
                /** Number of possible Cups **/



            }
        }
        databaseWater.addValueEventListener(getdata)
        databaseWater.addListenerForSingleValueEvent(getdata)

        var getdata2 =  object : ValueEventListener {

            override fun onCancelled(databaseError: DatabaseError) {

            }

            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var sb2 = StringBuilder()
                /** RESERVATION UPDATE **/
                for(i in dataSnapshot.children){
                    var amount = i.child("amount").value
                    var status = i.child("status").value
                    var userUid = i.child("userUid").value
                    var key = i.key

                    if(userUid.toString() == "$currentUserId"){
                        countreserv +=1
                        sb2.append("$status")

                        if(status.toString().equals("done", ignoreCase = true)){
                            Toast.makeText(this@SecondActivity, "Your reserved tea is ready!", Toast.LENGTH_LONG).show()
                        }


                        var amountstr = amount.toString().toInt()
                        var statusstr = status.toString()
                        var userUidstr = userUid.toString()

                        var struct = UserReservation(amountstr,statusstr,userUidstr)
                        databaseUsrReserv.child("$currentUserId").child("$key").setValue(struct)

                    }

                }


                /** RESERVATION UPDATE **/

            }
        }
        databaseReservSt.addValueEventListener(getdata2)
        databaseReservSt.addListenerForSingleValueEvent(getdata2)
        databaseUsrReserv.addValueEventListener(getdata2)
        databaseUsrReserv.addListenerForSingleValueEvent(getdata2)


        var getdata3 = object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                var count = snapshot.childrenCount
                var textstatres = findViewById(R.id.textstatres) as TextView
                textstatres.text = "$count x pending"
            }

        }
        databaseNrRes.addValueEventListener(getdata3)
        databaseNrRes.addListenerForSingleValueEvent(getdata3)


    }



    private fun setUpTabs() {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(StateFragment(), "KETTLE")
        adapter.addFragment(WaterFragment(), "WATER")

        val viewPager = findViewById(R.id.viewPager) as ViewPager
        viewPager.adapter = adapter
        val tabs = findViewById(R.id.tabs) as TabLayout
        tabs.setupWithViewPager(viewPager)
        tabs.getTabAt(0)
        tabs.getTabAt(1)
    }

    public fun onClicked(v: View?) {
        //Toast.makeText(this, "Let's Begin!", Toast.LENGTH_SHORT).show()
        var database = FirebaseDatabase.getInstance().getReference("kettle/status")
        val state = findViewById(R.id.state) as TextView
        val animation1 = AnimationUtils.loadAnimation(this, R.anim.scale_up)
        val animation2 = AnimationUtils.loadAnimation(this, R.anim.scale_down)
        if (v != null) {
            v.startAnimation(animation1)
            v.startAnimation(animation2)
            database.setValue("Idle")
            state.text="Idle"
        }
    }

    public fun offClicked(v: View?) {
        //Toast.makeText(this, "Let's Begin!", Toast.LENGTH_SHORT).show()
        var database = FirebaseDatabase.getInstance().getReference("kettle/status")
        val state = findViewById(R.id.state) as TextView
        val animation1 = AnimationUtils.loadAnimation(this, R.anim.scale_up)
        val animation2 = AnimationUtils.loadAnimation(this, R.anim.scale_down)
        if (v != null) {
            v.startAnimation(animation1)
            v.startAnimation(animation2)
            database.setValue("Off")
            state.text="Off"
        }

    }

    public fun startClicked(v: View?) {
        //Toast.makeText(this, "Let's Begin!", Toast.LENGTH_SHORT).show()
        var database = FirebaseDatabase.getInstance().getReference("kettle/brewing")
        val animation1 = AnimationUtils.loadAnimation(this, R.anim.scale_up)
        val animation2 = AnimationUtils.loadAnimation(this, R.anim.scale_down)
        if (v != null) {
            v.startAnimation(animation1)
            v.startAnimation(animation2)
            if(aval){
                database.setValue("true")
            }

            else if(!aval){
                Toast.makeText(this, "The kettle is occupied/turned off/without the min water required!", Toast.LENGTH_SHORT).show()
            }

        }
    }

    public fun reservClicked(v: View?) {
        val animation1 = AnimationUtils.loadAnimation(this, R.anim.scale_up)
        val animation2 = AnimationUtils.loadAnimation(this, R.anim.scale_down)

        //settings.visibility=View.VISIBLE
        if (v != null) {
            v.startAnimation(animation1)
            v.startAnimation(animation2)
                if(brew && !aval && curWater > 27) {
                    Toast.makeText(this, "Make your reservation!", Toast.LENGTH_SHORT).show()
                    val btninput = findViewById(R.id.btnForm) as ImageButton
                    val numberform = findViewById(R.id.editTextNumberDecimal) as EditText
                    btninput.isClickable = true
                    numberform.isClickable = true
                    btninput.visibility = View.VISIBLE
                    numberform.visibility = View.VISIBLE
                }

                else if(!brew && aval){
                    Toast.makeText(this, "The kettle is available and with enough water!", Toast.LENGTH_SHORT).show()
                }

        }
    }


    public fun inputForm(v: View?) {
        var databasein = FirebaseDatabase.getInstance().getReference("reservations")
        var currentUserId = FirebaseAuth.getInstance().uid

        val animation1 = AnimationUtils.loadAnimation(this, R.anim.scale_up)
        val animation2 = AnimationUtils.loadAnimation(this, R.anim.scale_down)
        //val btninput = findViewById(R.id.btnForm) as ImageButton
        val numberform = findViewById(R.id.editTextNumberDecimal) as EditText
        val btninput = findViewById(R.id.btnForm) as ImageButton
        //btninput.text = ""
        //settings.visibility=View.VISIBLE
        if (v != null) {
            v.startAnimation(animation1)
            v.startAnimation(animation2)


            var number = numberform.text.toString().toInt()

            var status = "pending"
            var userUid = currentUserId
            databasein.push().setValue(Reservation(number,status,userUid))




        }

        btninput.isClickable=false
        numberform.isClickable=false
        btninput.visibility=View.INVISIBLE
        numberform.visibility=View.INVISIBLE

    }

    public fun logOut(v: View?){
        firebaseAuth.signOut()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}