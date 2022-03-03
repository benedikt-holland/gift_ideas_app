import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.geschenkapp.ItemsViewModel
import com.example.geschenkapp.R
import java.util.*
import kotlin.collections.ArrayList

//class CustomAdapter(private var mList: ArrayList<ItemsViewModel>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>(), Filterable {
class CustomAdapter(private var testList: ArrayList<String>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>(), Filterable {

    //test array
    var testFilterList = ArrayList<String>()
    init {
        testFilterList = testList
    }
    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_design, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        //val ItemsViewModel = mList[position]
        //holder.bind(mList[position])
        holder.bind(testFilterList[position])

        // sets the image to the imageview from our itemHolder class
        //holder.imageView.setImageResource(ItemsViewModel.image)

        // sets the text to the textview from our itemHolder class
        //holder.textView.text = ItemsViewModel.text

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        //return mList.size
        return testFilterList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    testFilterList = testList
                } else {
                    val resultList = ArrayList<String>()
                    for (row in testList) {
                        if (row.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) {
                            resultList.add(row)
                        }
                    }
                    testFilterList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = testFilterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                testFilterList = results?.values as ArrayList<String>
                notifyDataSetChanged()
            }

        }
    }
    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        fun bind(mList: String) {
            //val imageView: ImageView = itemView.findViewById(R.id.imageview)
            val textView: TextView = itemView.findViewById(R.id.textView)
        }
    }


}
