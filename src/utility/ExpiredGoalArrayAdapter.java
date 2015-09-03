package utility;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.valentina.virtuallifecoach.R;
import com.example.valentina.virtuallifecoach.model.ExpiredGoal;

import java.util.List;

public class ExpiredGoalArrayAdapter extends ArrayAdapter<ExpiredGoal> {
    private final Activity context;
    private final List<ExpiredGoal> goals;

    public ExpiredGoalArrayAdapter(Activity context, List<ExpiredGoal> goals) {
        super(context, R.layout.layout_expiredgoal_row, goals);
        this.context = context;
        this.goals = goals;
    }

    static class ViewHolder {
        public ImageView satisfied;
        public TextView satisfiable;
        public TextView measureTypeName;
        public TextView deadline;
        public TextView range;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.layout_expiredgoal_row, null);

            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.measureTypeName = (TextView) rowView.findViewById(R.id.measuretypenametextview_adapterrow);
            viewHolder.deadline = (TextView) rowView.findViewById(R.id.deadlinetextview_adapterrow);
            viewHolder.range = (TextView) rowView.findViewById(R.id.rangetextview_adapterrow);
            viewHolder.satisfiable = (TextView) rowView.findViewById(R.id.satisfiabletextview_adapterrow);
            viewHolder.satisfied = (ImageView) rowView.findViewById(R.id.satisfiedimageview_adapterrow);

            rowView.setTag(viewHolder);
        }
        // fill data

        ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.measureTypeName.setText(goals.get(position).getMeasureType().getName());
        holder.deadline.setText(goals.get(position).getDeadline());
        holder.range.setText("[" + goals.get(position).getMinvalue() + ", " + goals.get(position).getMaxvalue() + "]");

        if(goals.get(position).isSatisfiable()){
            if( goals.get(position).isSatisfied()){
                holder.satisfied.setImageResource(R.drawable.like_black_24);
            } else{
                holder.satisfied.setImageResource(R.drawable.dislike_black_24);
            }
            holder.satisfiable.setVisibility(View.GONE);
        } else{
            holder.satisfiable.setText("NOT satisfiable");
            holder.satisfied.setVisibility(View.GONE);
        }

        return rowView;
    }
}
