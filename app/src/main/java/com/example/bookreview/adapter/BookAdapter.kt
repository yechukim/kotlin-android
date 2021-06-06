package com.example.bookreview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookreview.databinding.ItemBookBinding
import com.example.bookreview.model.Book

class BookAdapter(private val itemClickedListener: (Book) -> Unit) :
    ListAdapter<Book, BookAdapter.BookItemViewHolder>(diffUtil) {

    //안의 클래스 inner class
    //item_book 뷰 바인딩함
    inner class BookItemViewHolder(private val binding: ItemBookBinding) :
        RecyclerView.ViewHolder(binding.root) {

        //private으로 바인딩 선언하고 함수를 호출해서 외부에 접근
        fun bind(bookModel: Book) {
            binding.titleTextView.text = bookModel.title
            binding.descriptionTextView.text = bookModel.description

            binding.root.setOnClickListener {
                //루트 클릭시 이 함수 호출
                itemClickedListener(bookModel)
            }
            Glide
                .with(binding.coverImageView.context)// 뷰에 컨텍스트 있으니까
                .load(bookModel.coverSmallUrl)
                .into(binding.coverImageView)
        }
    }

    //view도 context 가지고 있음
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookItemViewHolder {
        // 미리 만들어진 뷰 홀더가 없을 때 새로 생성하는 함수
        return BookItemViewHolder(
            ItemBookBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BookItemViewHolder, position: Int) {
        //뷰홀더가 그려지게 되었을 때 데이터를 바인드 하는 함수
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<Book>() {
            override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}
