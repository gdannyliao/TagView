package view.sxdxz.liao.xingyu.myapplication

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.AppCompatTextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import view.sxdxz.liao.xingyu.view.TagView

/**
 * A placeholder fragment containing a simple view.
 */
class MainActivityFragment : Fragment() {
    private lateinit var inflater: LayoutInflater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inflater = LayoutInflater.from(context)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater?.inflate(R.layout.fragment_main, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view ?: return
        val layout = view.findViewById(R.id.tagView) as TagView
        for (i in 0..8) {
            val child = inflater.inflate(R.layout.item_tag, layout, false) as AppCompatTextView
            child.text = "test$i"
            layout.addView(child)
        }

    }
}
