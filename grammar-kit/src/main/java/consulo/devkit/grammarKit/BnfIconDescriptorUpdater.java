package consulo.devkit.grammarKit;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.ide.IconDescriptor;
import consulo.ide.IconDescriptorUpdater;
import consulo.ui.image.Image;
import org.intellij.grammar.BnfIcons;
import org.intellij.grammar.psi.BnfAttr;
import org.intellij.grammar.psi.BnfAttrs;
import org.intellij.grammar.psi.BnfModifier;
import org.intellij.grammar.psi.BnfRule;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 12.09.13.
 */
public class BnfIconDescriptorUpdater implements IconDescriptorUpdater
{
	@RequiredReadAction
	@Override
	public void updateIcon(@Nonnull IconDescriptor iconDescriptor, @Nonnull PsiElement element, int flags)
	{
		if(element instanceof BnfRule)
		{
			final Image base = hasModifier((BnfRule) element, "external") ? BnfIcons.EXTERNAL_RULE : BnfIcons.RULE;
			final Image visibility = hasModifier((BnfRule) element,"private") ? AllIcons.Nodes.C_private : AllIcons.Nodes.C_public;

			iconDescriptor.setMainIcon(base);
			iconDescriptor.setRightIcon(visibility);
		}
		else if(element instanceof BnfAttr)
		{
			iconDescriptor.setMainIcon(BnfIcons.ATTRIBUTE);
		}
		else if(element instanceof BnfAttrs)
		{
			iconDescriptor.setMainIcon(AllIcons.Nodes.Package);
		}
	}

	private static boolean hasModifier(BnfRule bnfRule, String modifier)
	{
		for(BnfModifier bnfModifier : bnfRule.getModifierList())
		{
			if(Comparing.equal(bnfModifier.getText(), modifier))
			{
				return true;
			}
		}
		return false;
	}
}
