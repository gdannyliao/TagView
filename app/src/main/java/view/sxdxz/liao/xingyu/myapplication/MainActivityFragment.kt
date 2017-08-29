package view.sxdxz.liao.xingyu.myapplication

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import view.sxdxz.liao.xingyu.view.TagView

/**
 * A placeholder fragment containing a simple view.
 */
class MainActivityFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val layout = inflater?.inflate(R.layout.fragment_main, container, false)
        val tagView = layout?.findViewById(R.id.tagView) as? TagView
        tagView?.setTags(arrayOf("标签1", "tag2", "test tag", "try", "love", "simple"))
        return layout
    }
}
