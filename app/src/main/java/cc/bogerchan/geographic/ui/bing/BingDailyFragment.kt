package cc.bogerchan.geographic.ui.bing


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.LinearLayout
import cc.bogerchan.geographic.GApplication
import cc.bogerchan.geographic.R
import cc.bogerchan.geographic.viewmodel.BingDailyViewModel
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.header.bezierlayout.BezierLayout


/**
 * A simple [Fragment] subclass.
 */
class BingDailyFragment : Fragment() {

    private lateinit var sdvImage: SimpleDraweeView
    private lateinit var wvContent: WebView
    private lateinit var trlContent: TwinklingRefreshLayout
    private lateinit var llError: LinearLayout
    private lateinit var llLoading: LinearLayout
    private val mBingDailyViewModel by lazy { ViewModelProviders.of(this).get(BingDailyViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadData()
        mBingDailyViewModel.prepareData()
    }

    private fun loadData() {
        mBingDailyViewModel.requestDailyData(GApplication.screenWidth, GApplication.screenHeight)
        mBingDailyViewModel.uiState.value = BingDailyViewModel.UIState.LOADING
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_bing_daily, container, false).apply {
            sdvImage = findViewById(R.id.sdv_fragment_bing_daily_image);
            wvContent = findViewById(R.id.wv_fragment_bing_daily_content)
            trlContent = findViewById(R.id.trl_fragment_bing_daily)
            llLoading = findViewById(R.id.ll_fragment_bing_daily_loading)
            llError = findViewById(R.id.ll_fragment_bing_daily_error)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViews()
        bindViewModels()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbindViewModels()
    }

    private fun bindViewModels() {
        mBingDailyViewModel.dailyImage.observe(this, Observer {
            when (it?.first) {
                BingDailyViewModel.ImageAction.NORMAL -> {
                    updateImage(it.second)
                }
                BingDailyViewModel.ImageAction.CLEAR -> {
                    clearImageCache(it.second)
                }
            }
        })
        mBingDailyViewModel.dailyHtmlText.observe(this, Observer {
            if (it == null) {
                when (mBingDailyViewModel.uiState.value) {
                    BingDailyViewModel.UIState.REFRESHING -> {
                        mBingDailyViewModel.uiState.value = BingDailyViewModel.UIState.STOP_REFRESH
                        mBingDailyViewModel.uiState.value = BingDailyViewModel.UIState.ERROR
                    }
                    else -> {
                        mBingDailyViewModel.uiState.value = BingDailyViewModel.UIState.ERROR
                    }
                }
            } else {
                updateHtmlText(it)
                when (mBingDailyViewModel.uiState.value) {
                    BingDailyViewModel.UIState.REFRESHING -> {
                        mBingDailyViewModel.uiState.value = BingDailyViewModel.UIState.STOP_REFRESH
                        mBingDailyViewModel.uiState.value = BingDailyViewModel.UIState.NORMAL
                    }
                    else -> {
                        mBingDailyViewModel.uiState.value = BingDailyViewModel.UIState.NORMAL
                    }
                }
            }
        })
        mBingDailyViewModel.uiState.observe(this, Observer {
            when (it) {
                BingDailyViewModel.UIState.LOADING -> {
                    llLoading.visibility = View.VISIBLE
                    llError.visibility = View.INVISIBLE
                    trlContent.visibility = View.INVISIBLE
                }
                BingDailyViewModel.UIState.NORMAL -> {
                    llLoading.visibility = View.INVISIBLE
                    llError.visibility = View.INVISIBLE
                    trlContent.visibility = View.VISIBLE
                }
                BingDailyViewModel.UIState.STOP_REFRESH -> {
                    llLoading.visibility = View.INVISIBLE
                    llError.visibility = View.INVISIBLE
                    trlContent.visibility = View.VISIBLE
                    trlContent.finishRefreshing()
                }
                BingDailyViewModel.UIState.ERROR -> {
                    llLoading.visibility = View.INVISIBLE
                    llError.visibility = View.VISIBLE
                    trlContent.visibility = View.INVISIBLE
                }
                else -> {
                }
            }
        })
    }

    private fun clearImageCache(imageURL: String) {
        Fresco.getImagePipeline().evictFromCache(Uri.parse(imageURL))
    }

    private fun updateHtmlText(htmlText: String) {
        wvContent.loadData(htmlText, "text/html; charset=UTF-8", null)
        wvContent.visibility = View.VISIBLE
    }

    private fun updateImage(imgURL: String) {
        sdvImage.controller = Fresco.newDraweeControllerBuilder().apply {
            imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(imgURL)).apply {
                isProgressiveRenderingEnabled = true
            }.build()
            oldController = sdvImage.controller
        }.build()
        sdvImage.visibility = View.VISIBLE
    }

    private fun unbindViewModels() {
        mBingDailyViewModel.dailyImage.removeObservers(this)
        mBingDailyViewModel.dailyHtmlText.removeObservers(this)
        mBingDailyViewModel.uiState.removeObservers(this)
    }

    private fun initViews() {
        wvContent.setBackgroundColor(Color.TRANSPARENT)
        wvContent.settings.apply {
            defaultTextEncodingName = "UTF-8"
//            textZoom = 110
//            useWideViewPort = true
//            loadWithOverviewMode = true
//            layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
            javaScriptEnabled = true
        }
        wvContent.setOnTouchListener { _, _ -> true }
        trlContent.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                sdvImage.layoutParams = (sdvImage.layoutParams as FrameLayout.LayoutParams).apply {
                    width = FrameLayout.LayoutParams.MATCH_PARENT
                    height = trlContent.height - topMargin - bottomMargin * 2
                }
                trlContent.viewTreeObserver.removeOnPreDrawListener(this)
                return true
            }
        })
        trlContent.setEnableRefresh(true)
        trlContent.setEnableLoadmore(false)
        val bezierHeaderView = BezierLayout(activity)
        bezierHeaderView.setWaveColor(ResourcesCompat.getColor(resources, R.color.colorPrimaryDark20, activity.theme))
        bezierHeaderView.setRippleColor(ResourcesCompat.getColor(resources, R.color.colorPrimaryDark20, activity.theme))
        trlContent.setHeaderView(bezierHeaderView)
        trlContent.setOnRefreshListener(object : RefreshListenerAdapter() {
            override fun onRefresh(refreshLayout: TwinklingRefreshLayout?) {
                mBingDailyViewModel.uiState.value = BingDailyViewModel.UIState.REFRESHING
                mBingDailyViewModel.requestDailyData(GApplication.screenWidth, GApplication.screenHeight)
            }
        })
        llError.setOnClickListener {
            mBingDailyViewModel.uiState.value = BingDailyViewModel.UIState.LOADING
            mBingDailyViewModel.requestDailyData(GApplication.screenWidth, GApplication.screenHeight)
        }
    }
}