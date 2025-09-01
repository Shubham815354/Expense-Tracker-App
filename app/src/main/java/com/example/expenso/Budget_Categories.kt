package com.example.expenso

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expenso.ApiUsage.API
import com.example.expenso.ApiUsage.Constant
import com.example.expenso.ApiUsage.obj
import com.example.expenso.Data_Class.ApiCategoryPostResponse
import com.example.expenso.Data_Class.CategoryItem
import com.example.expenso.Data_Class.CategoryResponse
import com.example.expenso.Data_Class.Fetch_CategoryId
import com.example.expenso.Data_Class.UserCategoryResponse
import com.example.expenso.adaptor.CategoryAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.material.bottomsheet.BottomSheetDialog


class Budget_Categories : AppCompatActivity() {

    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_categories)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_nav)
        bottomNavigationView.selectedItemId = R.id.nav_category

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_report->{
                    startActivity(Intent(this, Expenses::class.java))
                    item.icon?.setTint(Color.parseColor("#0000FF"))
                    return@setOnItemSelectedListener true
                }
                R.id.nav_category -> {
                    // Open Budget Categories Activity
                    startActivity(Intent(this, Budget_Categories::class.java))

                    return@setOnItemSelectedListener true

                }
                R.id.nav_profile -> {
                    // Open Profile Activity
                    startActivity(Intent(this, Profile::class.java))
                    return@setOnItemSelectedListener true
                }
                R.id.nav_report->{
                    startActivity(Intent(this, Expenses::class.java))
                    return@setOnItemSelectedListener true
                }

                else -> false
            }
        }

        val addCategoryButton: ImageButton = findViewById(R.id.add_category_button)
        addCategoryButton.setOnClickListener {
            showAddCategoryBottomSheet()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 4)

        adapter = CategoryAdapter(mutableListOf()) { categoryItem ->
            showAddCategoryBottomSheet(categoryItem) // Open in delete mode
        }
        recyclerView.adapter = adapter

        setupDefaultCategory()
        val token = Constant.getSavedToken(this)
        setupUserCategories(token.toString())
    }

    private fun showAddCategoryBottomSheet(item: CategoryItem? = null) {
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_add_category, null)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(bottomSheetView)

        val editText = bottomSheetView.findViewById<EditText>(R.id.edit_category_name)
        val addButton = bottomSheetView.findViewById<Button>(R.id.btn_add_category)

        val token = Constant.getSavedToken(this)

        if (item != null) {
            editText.setText(item.title)
            editText.isEnabled = false
            if (item.isUserCreated) {
                addButton.text = "Delete"
            } else {
                addButton.text = "Default Category"
                addButton.isEnabled = false
            }
        } else {
            addButton.text = "Add"
        }

        addButton.setOnClickListener {
            if (item != null && item.isUserCreated) {
                val categoryId = item.id
                if (categoryId != null) {
                    deleteCategory(categoryId, token.toString())
                    bottomSheetDialog.dismiss()
                } else {
                    Toast.makeText(this@Budget_Categories, "Category ID is missing", Toast.LENGTH_SHORT).show()
                }
            } else if (item == null) {
                // Handle adding a new category here if required.
                // You may want to use a different function for adding a category
                val categoryName = editText.text.toString()
                if (categoryName.isNotBlank()) {
                    createNewCategory(categoryName, token.toString())
                    bottomSheetDialog.dismiss()
                } else {
                    Toast.makeText(this@Budget_Categories, "Category name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
        }

        bottomSheetDialog.show()
    }

    private fun createNewCategory(name: String, token: String) {
        val retrofit = obj.createService(API::class.java)
        retrofit.add_user_categories(token, name)
            .enqueue(object : Callback<ApiCategoryPostResponse> {
                override fun onResponse(call: Call<ApiCategoryPostResponse>, response: Response<ApiCategoryPostResponse>) {
                    if (response.isSuccessful) {
                        val createdCategory = response.body()?.category
                        if (createdCategory != null) {
                            val newCategoryItem = CategoryItem(
                                title = createdCategory.name,
                                iconUrl = null,
                                firstLetter = createdCategory.name.firstOrNull() ?: '?',
                                isUserCreated = true,
                                id = createdCategory._id
                            )
                            adapter.addItem(newCategoryItem)
                            val allCategories = Constant.getCategories(this@Budget_Categories, "user_defined_categories").toMutableList()
                            allCategories.add(
                                Fetch_CategoryId(
                                    _id = createdCategory._id,
                                    name = createdCategory.name
                                )
                            )
                            Constant.saveCategories(this@Budget_Categories, allCategories, "user_defined_categories")

                            Toast.makeText(this@Budget_Categories, "Category added!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.d("AddCategoryError", "Code: ${response.code()}, Message: ${response.errorBody()?.string()}")
                        Toast.makeText(this@Budget_Categories, "Failed to add category", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiCategoryPostResponse>, t: Throwable) {
                    Toast.makeText(this@Budget_Categories, "Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun deleteCategory(categoryId: String, token: String) {
        val retrofit = obj.createService(API::class.java)
        Log.d("DeleteCategory", "Attempting to delete category with ID: $categoryId")
        retrofit.deleteUserCategory(categoryId, token).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@Budget_Categories, "Category deleted", Toast.LENGTH_SHORT).show()
                    val position = adapter.removeItemById(categoryId)
                    if (position != -1) adapter.notifyItemRemoved(position)
                    val allCategories = Constant.getCategories(this@Budget_Categories,"user_defined_categories") // Retrieve updated categories
                    Constant.saveCategories(this@Budget_Categories, allCategories, "user_categories")
                } else {
                    Toast.makeText(this@Budget_Categories, "Failed to delete", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@Budget_Categories, t.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun setupUserCategories(token: String) {
        val retrofit = obj.createService(API::class.java)
        retrofit.get_user_categories(token).enqueue(object : Callback<UserCategoryResponse> {
            override fun onResponse(call: Call<UserCategoryResponse>, response: Response<UserCategoryResponse>) {
                if (response.isSuccessful) {
                    val userCategories = response.body()?.categories ?: emptyList()

                    val categoryItems = userCategories.map {
                        CategoryItem(
                            title = it.name,
                            iconUrl = it.icon.toString(),
                            firstLetter = it.name.firstOrNull() ?: '?',
                            id = it._id,
                            isUserCreated = true
                        )
                    }

                    adapter.addAll(categoryItems)
                    val categoryIdList = categoryItems.mapNotNull { item ->
                        item.id?.let {
                            Fetch_CategoryId(
                                _id = it,
                                name = item.title,
                                icon = item.iconUrl // ← Save the image URL too
                            )
                        }
                    }
                    Constant.saveCategories(this@Budget_Categories, categoryIdList, "user_defined_categories")
                }
            }

            override fun onFailure(call: Call<UserCategoryResponse>, t: Throwable) {
                Toast.makeText(this@Budget_Categories, t.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun setupDefaultCategory() {
        val retrofit = obj.createService(API::class.java)
        retrofit.get_default_categories().enqueue(object : Callback<CategoryResponse> {
            override fun onResponse(call: Call<CategoryResponse?>, response: Response<CategoryResponse?>) {
                if (response.isSuccessful) {
                    val temp = response.body()
                    temp?.category?.let { categoryList ->
                        val baseUrl = "https://expensio-nkvc.onrender.com/"
                        val categoryItems = categoryList.map {
                            CategoryItem(
                                title = it.name,
                                iconUrl = baseUrl + it.icon?.replace("\\", "/"),
                                firstLetter = it.name.firstOrNull() ?: '?',
                                isUserCreated = false,
                                id = it._id
                            )


                        }
                        adapter.addAll(categoryItems)
                        val categoryIdList = categoryItems.mapNotNull { item ->
                            item.id?.let {
                                Fetch_CategoryId(
                                    _id = it,
                                    name = item.title,
                                    icon = item.iconUrl // ← Save the image URL too
                                )
                            }
                        }
                        Constant.saveCategories(this@Budget_Categories, categoryIdList, "predefined_categories")

                    }
                }
            }

            override fun onFailure(call: Call<CategoryResponse?>, t: Throwable) {
                Toast.makeText(this@Budget_Categories, t.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }
}
