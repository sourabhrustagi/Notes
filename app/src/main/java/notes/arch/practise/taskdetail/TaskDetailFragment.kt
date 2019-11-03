package notes.arch.practise.taskdetail

import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerFragment
import notes.arch.practise.EventObserver
import notes.arch.practise.R
import notes.arch.practise.databinding.TaskdetailFragBinding
import notes.arch.practise.tasks.DELETE_RESULT_OK
import notes.arch.practise.util.setupRefreshLayout
import notes.arch.practise.util.setupSnackbar
import javax.inject.Inject

/**
 * Main UI for the task detail screen.
 */
class TaskDetailFragment : DaggerFragment() {
    lateinit var viewDataBinding: TaskdetailFragBinding

    private val args: TaskDetailFragmentArgs by navArgs()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<TaskDetailViewModel> { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.taskdetail_frag, container, false)
        viewDataBinding = TaskdetailFragBinding.bind(view).apply {
            viewmodel = viewModel
        }
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        viewModel.start(args.taskId)
        setHasOptionsMenu(true)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupFab()
        view?.setupSnackbar(this, viewModel.snackbarMessage, Snackbar.LENGTH_SHORT)
        setupNavigation()
        this.setupRefreshLayout(viewDataBinding.refreshLayout)
    }

    private fun setupNavigation() {
        viewModel.deleteTaskCommand.observe(this, EventObserver {
            val action = TaskDetailFragmentDirections
                .actionTaskDetailFragmentToTasksFragment(DELETE_RESULT_OK)
            findNavController().navigate(action)
        })
        viewModel.editTaskCommand.observe(this, EventObserver {
            val action = TaskDetailFragmentDirections
                .actionTaskDetailFragmentToAddEditTaskFragment(
                    args.taskId,
                    resources.getString(R.string.edit_task)
                )
            findNavController().navigate(action)
        })
    }

    private fun setupFab() {
        activity?.findViewById<View>(R.id.fab_edit_task)?.setOnClickListener {
            viewModel.editTask()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.taskdetail_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_delete -> {
                viewModel.deleteTask()
                true
            }
            else -> false
        }
    }
}