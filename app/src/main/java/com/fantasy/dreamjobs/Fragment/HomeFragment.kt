package com.fantasy.dreamjobs.Fragment


import android.accessibilityservice.GestureDescription
import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fantasy.dreamjobs.AdapterPostedJobs
import com.fantasy.dreamjobs.ModelPostedJobs
import com.fantasy.dreamjobs.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception



class HomeFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private  lateinit var databaseReference : DatabaseReference
    private lateinit var uid : String
   private lateinit var recyclerView: RecyclerView
   private lateinit var arrayList : ArrayList<ModelPostedJobs>
   private lateinit var adapterJobs : AdapterPostedJobs
   private lateinit var imgPro : CircleImageView
   private lateinit var searchJobs : EditText
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arrayList=ArrayList<ModelPostedJobs>()
        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()
        recyclerView=view.findViewById(R.id.recyclerJobs)
        searchJobs=view.findViewById(R.id.searchJobs)
        recyclerView.layoutManager=LinearLayoutManager(context)
        imgPro=view.findViewById(R.id.imgPro)

        if (checkNetwork()) {
        }
        else if (!checkNetwork()) {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Ohh fuck!")
            // Set Alert Title
            builder.setTitle("No internet connection")
            builder.setPositiveButton("OK",null)
            val alertDialog = builder.create()
            alertDialog.show()

        }
        // for fetching img on mainActivity //
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        if (uid.isNotEmpty()) {
            getUserData()
        }
        databaseReference = FirebaseDatabase.getInstance().reference.child("Users").child("JobPosted")
        databaseReference.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                arrayList.clear()
                for(i in snapshot.children){
                    val obj=ModelPostedJobs(
                        i.child("jobTitle").value.toString(),
                        i.child("location").value.toString(),
                        i.child("description").value.toString(),
                        i.child("time").value.toString(),
                    )
                    arrayList.add(obj)
                }
                adapterJobs=AdapterPostedJobs(this@HomeFragment,arrayList)
                recyclerView.adapter=adapterJobs
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        searchJobs.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchJob(p0.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }

  private fun checkNetwork() : Boolean{
      return try {
          val command = "ping -c 1 google.com"
          Runtime.getRuntime().exec(command).waitFor() == 0
      } catch (e: Exception) {
          false
      }
  }

    private fun getUserData() {
        databaseReference.child(uid).addValueEventListener(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
//
                if(snapshot.child("imgUrl").exists()){
                    context?.let {
                        Glide.with(it)
                            .load(snapshot.child("imgUrl").value.toString())
                            .into(imgPro)
                    }
                }else{
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun searchJob(jobs: String) {
        val query= FirebaseDatabase.getInstance().reference.child("Users").child("JobPosted").orderByChild("jobTitle")
            .startAt(jobs)
            .endAt(jobs+"\uf8ff")
        query.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                arrayList.clear()
                for(i in snapshot.children){
                    val obj=ModelPostedJobs(
                        i.child("jobTitle").value.toString(),
                        i.child("location").value.toString(),
                        i.child("description").value.toString(),
                        i.child("time").value.toString(),
                    )
                   arrayList.add(obj)

                }
                adapterJobs=AdapterPostedJobs(this@HomeFragment,arrayList)
                recyclerView.adapter=adapterJobs
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }
}