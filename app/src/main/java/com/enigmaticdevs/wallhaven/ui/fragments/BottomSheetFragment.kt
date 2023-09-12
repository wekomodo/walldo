package com.enigmaticdevs.wallhaven.ui.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.data.model.Photo
import com.enigmaticdevs.wallhaven.databinding.BottomsheetFragmentBinding
import com.enigmaticdevs.wallhaven.ui.search.SearchActivity
import com.enigmaticdevs.wallhaven.util.getSerializable
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip


class BottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: BottomsheetFragmentBinding
    private var photo : Photo?  = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottomsheet_fragment, container, false)
        binding = BottomsheetFragmentBinding.bind(view)
        try {
            photo  = getSerializable(requireArguments(),"wallpaperInfo",Photo::class.java)
        }catch (e : Exception){
            println(e.localizedMessage)
        }
        photo?.let { wallpaper ->
            binding.apply {
                infoImageId.text = wallpaper.data.id
                infoResolution.text = wallpaper.data.resolution
                infoFavorites.text = wallpaper.data.favorites.toString()
                val size = (wallpaper.data.file_size / 1024).toString() + "kb"
                infoSize.text = size
                infoCreatedAt.text = wallpaper.data.created_at
                val colors = wallpaper.data.colors
                binding.apply {
                    bindData(color0, color0Text, colors[0])
                    bindData(color1, color1Text, colors[1])
                    bindData(color2, color2Text, colors[2])
                    bindData(color3, color3Text, colors[3])
                    bindData(color4, color4Text, colors[4])
                }
                infoImageId.setOnClickListener {
                    copyColor(binding.infoImageId.text.toString())
                }
            }
            val tf = Typeface.createFromAsset(
                requireContext().assets,
                "fonts/gordita_regular.otf"
            )
            for (tags in wallpaper.data.tags) {
                val chip = Chip(requireContext())
                chip.text = tags.name
                chip.isClickable = true
                binding.tagsGroup.addView(chip)
                chip.typeface = tf
                chip.setOnClickListener {
                    val tag = chip.text
                    val intent = Intent(requireContext(), SearchActivity::class.java)
                    intent.putExtra("tag", tag)
                    parentFragmentManager.popBackStack()
                    startActivity(intent)
                }
            }
        }

        return view
    }
    private fun bindData(cardColor: CardView, colorText: TextView, color: String) {
        cardColor.setCardBackgroundColor(Color.parseColor(color))
        colorText.apply {
            text = color
            setOnClickListener {
                copyColor(color)
            }
        }

    }

    private fun copyColor(color: String) {
        val clipboardManager =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText(R.string.app_name.toString(), color)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(requireContext(), "Copied", Toast.LENGTH_SHORT).show()

    }

    companion object {
        @JvmStatic
        fun newInstance(photo: Photo): BottomSheetFragment {
            val fragment = BottomSheetFragment()
            val bundle = Bundle()
            bundle.putSerializable("wallpaperInfo", photo)
            fragment.arguments = bundle
            return fragment
        }
    }
}
